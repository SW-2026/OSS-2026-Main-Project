package com.wit.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wit.ai.client.ComfyUIClient;
import com.wit.ai.client.ComfyUIClientException;
import com.wit.ai.client.ComfyUIResult;
import com.wit.ai.config.ComfyUIProperties;
import com.wit.ai.domain.AiTask;
import com.wit.ai.domain.CharacterAsset;
import com.wit.ai.dto.AiPanelsGenerateRequest;
import com.wit.ai.dto.BackgroundMention;
import com.wit.ai.dto.CharacterMention;
import com.wit.ai.dto.ComposedPrompt;
import com.wit.ai.dto.ScenarioPanel;
import com.wit.ai.repository.AiTaskRepository;
import com.wit.ai.repository.CharacterAssetRepository;
import com.wit.ai.storage.ImageStorage;
import com.wit.ai.storage.StoredImage;
import com.wit.ai.workflow.WorkflowParams;
import com.wit.ai.workflow.WorkflowTemplateLoader;
import com.wit.episode.domain.Episode;
import com.wit.episode.domain.Panel;
import com.wit.episode.domain.PanelStatus;
import com.wit.episode.repository.EpisodeRepository;
import com.wit.episode.repository.PanelRepository;
import com.wit.model.domain.CharacterModel;
import com.wit.model.repository.CharacterModelRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComfyUIOrchestrator {

    private static final String CHARACTER_WORKFLOW = "character.json";
    private static final String CHARACTER_CATEGORY = "character";
    private static final String PANEL_CATEGORY = "panel";

    private final CharacterModelRepository characterModelRepository;
    private final AiTaskRepository aiTaskRepository;
    private final CharacterAssetRepository characterAssetRepository;
    private final PromptComposer promptComposer;
    private final WorkflowTemplateLoader workflowTemplateLoader;
    private final ComfyUIClient comfyUIClient;
    private final ImageStorage imageStorage;
    private final ComfyUIProperties comfyUIProperties;
    private final EpisodeRepository episodeRepository;
    private final PanelRepository panelRepository;
    private final ScenarioAnalyzer scenarioAnalyzer;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional
    public void processCharacterGeneration(Long taskId, Long modelId,
                                           String poseTags, String backgroundTags) {
        AiTask task = aiTaskRepository.findById(taskId).orElse(null);
        if (task == null) {
            log.error("ComfyUIOrchestrator: AiTask not found, taskId={}", taskId);
            return;
        }

        String stage = "init";
        try {
            stage = "loadModel";
            CharacterModel model = characterModelRepository.findById(modelId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "CharacterModel not found: " + modelId));

            task.markProcessing();
            aiTaskRepository.save(task);

            stage = "compose";
            ScenarioPanel fakePanel = new ScenarioPanel(
                    1, "character generation", model.getModelId(),
                    null, null, poseTags, backgroundTags, null);
            CharacterMention fakeMention = new CharacterMention(
                    model.getModelName(), model.getModelId(), model.getTriggerWord(),
                    model.getLoraModelPath());
            ComposedPrompt composed = promptComposer.compose(fakePanel, fakeMention);

            stage = "loadWorkflow";
            WorkflowParams params = new WorkflowParams(
                    composed.positivePrompt(),
                    composed.negativePrompt(),
                    composed.seed(),
                    composed.loraName()
            );
            String workflowJson = workflowTemplateLoader.load(CHARACTER_WORKFLOW, params);

            stage = "submit";
            String promptId = comfyUIClient.submitWorkflow(workflowJson);

            stage = "poll";
            ComfyUIResult result = pollUntilComplete(promptId);

            stage = "download";
            byte[] imageBytes = comfyUIClient.downloadImage(result.filename(), result.subfolder());

            stage = "save";
            StoredImage stored = imageStorage.save(imageBytes, CHARACTER_CATEGORY, null);

            stage = "dbSave";
            CharacterAsset asset = characterAssetRepository.save(
                    CharacterAsset.builder()
                            .characterModel(model)
                            .project(model.getProject())
                            .imageUrl(stored.accessUrl())
                            .finalPrompt(composed.positivePrompt())
                            .seed(composed.seed())
                            .build()
            );

            task.markCompleted("CharacterAsset", asset.getAssetId(), stored.accessUrl());
            aiTaskRepository.save(task);

        } catch (Exception e) {
            String message = "[" + stage + "] " + e.getMessage();
            log.error("ComfyUIOrchestrator failed: {}", message, e);
            task.markFailed(message);
            aiTaskRepository.save(task);
        }
    }

    @Async
    @Transactional
    public void processPanelGeneration(Long taskId, Long episodeId,
                                       AiPanelsGenerateRequest request) {
        AiTask task = aiTaskRepository.findById(taskId).orElse(null);
        if (task == null) {
            log.error("ComfyUIOrchestrator: AiTask not found, taskId={}", taskId);
            return;
        }

        String masterStage = "init";
        List<Integer> failedOrders = new ArrayList<>();

        try {
            masterStage = "loadEpisode";
            Episode episode = episodeRepository.findById(episodeId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Episode not found: " + episodeId));

            task.markProcessing();
            task.updateProgressPercent(0);
            aiTaskRepository.save(task);

            masterStage = "buildMentionMap";
            Map<Long, CharacterMention> mentionByModelId = new HashMap<>();
            if (request.characters() != null) {
                List<Long> modelIds = request.characters().stream()
                        .map(CharacterMention::modelId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
                Map<Long, CharacterModel> modelById = modelIds.isEmpty()
                        ? Map.of()
                        : characterModelRepository.findAllById(modelIds).stream()
                                .collect(Collectors.toMap(CharacterModel::getModelId, m -> m));
                for (CharacterMention m : request.characters()) {
                    if (m.modelId() != null) {
                        CharacterModel cm = modelById.get(m.modelId());
                        String loraPath = (cm != null) ? cm.getLoraModelPath() : null;
                        mentionByModelId.put(m.modelId(),
                                new CharacterMention(m.name(), m.modelId(), m.triggerWord(), loraPath));
                    }
                }
            }

            masterStage = "analyze";
            List<ScenarioPanel> scenarioPanels =
                    scenarioAnalyzer.analyze(request.scenarioText(), request.characters(),
                            request.backgrounds());

            masterStage = "panelsInit";
            List<Panel> panels = new ArrayList<>(scenarioPanels.size());
            for (ScenarioPanel sp : scenarioPanels) {
                Panel panel = Panel.builder()
                        .panelOrder(sp.panelOrder())
                        .status(PanelStatus.PENDING)
                        .scenarioText(sp.panelScenario())
                        .extractedParams(serializeScenarioPanel(sp))
                        .backgroundAssetId(sp.backgroundAssetId())
                        .build();
                episode.addPanel(panel);
                panels.add(panel);
            }
            panelRepository.saveAll(panels);

            masterStage = "panelsLoop";
            int total = panels.size();
            for (int idx = 0; idx < total; idx++) {
                Panel panel = panels.get(idx);
                ScenarioPanel sp = scenarioPanels.get(idx);
                CharacterMention mention = (sp.characterModelId() != null)
                        ? mentionByModelId.get(sp.characterModelId())
                        : null;
                try {
                    processOnePanel(panel, sp, mention);
                } catch (Exception e) {
                    failedOrders.add(sp.panelOrder());
                    panel.markFailed("[panel:" + sp.panelOrder() + "] " + e.getMessage());
                    panelRepository.save(panel);
                }
                task.updateProgressPercent(((idx + 1) * 100) / total);
                aiTaskRepository.save(task);
            }

            masterStage = "finalize";
            if (failedOrders.isEmpty()) {
                task.markCompleted("Episode", episodeId, null);
            } else {
                task.markFailed("[panels-partial-failed] panels=" + failedOrders
                        + " (" + failedOrders.size() + "/" + total + " failed)");
            }
            aiTaskRepository.save(task);

        } catch (Exception e) {
            String message = "[" + masterStage + "] " + e.getMessage();
            log.error("ComfyUIOrchestrator processPanelGeneration failed: {}", message, e);
            task.markFailed(message);
            aiTaskRepository.save(task);
        }
    }

    // 1컷 생성 — 새 컷 1개 append 후 기존 processOnePanel 재사용. processPanelGeneration과 독립
    @Async
    @Transactional
    public void processSinglePanelGeneration(Long taskId, Long episodeId,
                                             Long characterModelId, Long backgroundAssetId,
                                             String scenarioText) {
        AiTask task = aiTaskRepository.findById(taskId).orElse(null);
        if (task == null) {
            log.error("ComfyUIOrchestrator: AiTask not found, taskId={}", taskId);
            return;
        }
        String stage = "init";
        try {
            stage = "loadEpisode";
            Episode episode = episodeRepository.findById(episodeId)
                    .orElseThrow(() -> new EntityNotFoundException("Episode not found: " + episodeId));
            task.markProcessing();
            task.updateProgressPercent(0);
            aiTaskRepository.save(task);

            stage = "buildMention";
            CharacterMention mention = null;
            if (characterModelId != null) {
                CharacterModel cm = characterModelRepository.findById(characterModelId)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "CharacterModel not found: " + characterModelId));
                mention = new CharacterMention(cm.getModelName(), cm.getModelId(),
                        cm.getTriggerWord(), cm.getLoraModelPath());
            }
            BackgroundMention background = (backgroundAssetId != null)
                    ? new BackgroundMention(null, backgroundAssetId, null) : null;

            stage = "analyzeSingle";
            ScenarioPanel sp = scenarioAnalyzer.analyzeSingle(scenarioText, mention, background);

            stage = "panelInit";
            int lastOrder = panelRepository.findMaxOrderByEpisodeId(episodeId).orElse(0);
            Panel panel = Panel.builder()
                    .panelOrder(lastOrder + 1)
                    .status(PanelStatus.PENDING)
                    .scenarioText(sp.panelScenario())
                    .extractedParams(serializeScenarioPanel(sp))
                    .backgroundAssetId(backgroundAssetId)
                    .build();
            episode.addPanel(panel);
            panelRepository.save(panel);

            stage = "generate";
            processOnePanel(panel, sp, mention);

            task.updateProgressPercent(100);
            task.markCompleted("Panel", panel.getPanelId(), panel.getFinalImageUrl());
            aiTaskRepository.save(task);
        } catch (Exception e) {
            String message = "[" + stage + "] " + e.getMessage();
            log.error("ComfyUIOrchestrator processSinglePanelGeneration failed: {}", message, e);
            task.markFailed(message);
            aiTaskRepository.save(task);
        }
    }

    private void processOnePanel(Panel panel, ScenarioPanel sp, CharacterMention mention) {
        String panelStage = "compose";
        try {
            ComposedPrompt composed = promptComposer.compose(sp, mention);

            panelStage = "loadWorkflow";
            WorkflowParams params = new WorkflowParams(
                    composed.positivePrompt(),
                    composed.negativePrompt(),
                    composed.seed(),
                    composed.loraName()
            );
            String workflowJson = workflowTemplateLoader.load(CHARACTER_WORKFLOW, params);

            panelStage = "submit";
            String promptId = comfyUIClient.submitWorkflow(workflowJson);

            panelStage = "poll";
            ComfyUIResult result = pollUntilComplete(promptId);

            panelStage = "download";
            byte[] imageBytes = comfyUIClient.downloadImage(result.filename(), result.subfolder());

            panelStage = "save";
            StoredImage stored = imageStorage.save(imageBytes, PANEL_CATEGORY, null);

            panelStage = "dbSave";
            if (mention != null) {
                CharacterModel charModel = characterModelRepository.findById(mention.modelId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "CharacterModel not found: " + mention.modelId()));
                CharacterAsset asset = characterAssetRepository.save(
                        CharacterAsset.builder()
                                .characterModel(charModel)
                                .project(charModel.getProject())
                                .imageUrl(stored.accessUrl())
                                .finalPrompt(composed.positivePrompt())
                                .seed(composed.seed())
                                .build()
                );
                panel.updateCharacterAssetId(asset.getAssetId());
            }
            panel.updateFinalImageUrl(stored.accessUrl());
            panel.updateFinalPrompt(composed.positivePrompt());
            panel.updateSeed(composed.seed());
            panel.updateStatus(PanelStatus.COMPLETED);
            panelRepository.save(panel);

        } catch (Exception e) {
            throw new RuntimeException(panelStage + ":" + e.getMessage(), e);
        }
    }

    private String serializeScenarioPanel(ScenarioPanel sp) {
        try {
            return objectMapper.writeValueAsString(sp);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private ComfyUIResult pollUntilComplete(String promptId) {
        int maxAttempts = comfyUIProperties.maxPollAttempts();
        long intervalMillis = comfyUIProperties.pollIntervalMillis();
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Optional<ComfyUIResult> result = comfyUIClient.pollResult(promptId);
            if (result.isPresent()) {
                return result.get();
            }
            sleep(intervalMillis);
        }
        throw new ComfyUIClientException(
                "Polling timeout after " + maxAttempts + " attempts (promptId=" + promptId + ")");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ComfyUIClientException("Polling interrupted", e);
        }
    }
}
