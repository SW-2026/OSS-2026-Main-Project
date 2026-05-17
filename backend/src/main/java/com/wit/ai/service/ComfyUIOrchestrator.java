package com.wit.ai.service;

import com.wit.ai.client.ComfyUIClient;
import com.wit.ai.client.ComfyUIClientException;
import com.wit.ai.client.ComfyUIResult;
import com.wit.ai.config.ComfyUIProperties;
import com.wit.ai.domain.AiTask;
import com.wit.ai.domain.CharacterAsset;
import com.wit.ai.dto.AiPanelsGenerateRequest;
import com.wit.ai.dto.CharacterMention;
import com.wit.ai.dto.ComposedPrompt;
import com.wit.ai.dto.ScenarioPanel;
import com.wit.ai.repository.AiTaskRepository;
import com.wit.ai.repository.CharacterAssetRepository;
import com.wit.ai.storage.ImageStorage;
import com.wit.ai.storage.StoredImage;
import com.wit.ai.workflow.WorkflowParams;
import com.wit.ai.workflow.WorkflowTemplateLoader;
import com.wit.model.domain.CharacterModel;
import com.wit.model.repository.CharacterModelRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComfyUIOrchestrator {

    private static final String WORKFLOW_TEMPLATE = "character.json";
    private static final String STORAGE_CATEGORY = "character";

    private final CharacterModelRepository characterModelRepository;
    private final AiTaskRepository aiTaskRepository;
    private final CharacterAssetRepository characterAssetRepository;
    private final PromptComposer promptComposer;
    private final WorkflowTemplateLoader workflowTemplateLoader;
    private final ComfyUIClient comfyUIClient;
    private final ImageStorage imageStorage;
    private final ComfyUIProperties comfyUIProperties;

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
                    model.getModelName(), model.getModelId(), model.getTriggerWord());
            ComposedPrompt composed = promptComposer.compose(fakePanel, fakeMention);

            stage = "loadWorkflow";
            WorkflowParams params = new WorkflowParams(
                    composed.positivePrompt(),
                    composed.negativePrompt(),
                    composed.seed(),
                    composed.loraName()
            );
            String workflowJson = workflowTemplateLoader.load(WORKFLOW_TEMPLATE, params);

            stage = "submit";
            String promptId = comfyUIClient.submitWorkflow(workflowJson);

            stage = "poll";
            ComfyUIResult result = pollUntilComplete(promptId);

            stage = "download";
            byte[] imageBytes = comfyUIClient.downloadImage(result.filename(), result.subfolder());

            stage = "save";
            StoredImage stored = imageStorage.save(imageBytes, STORAGE_CATEGORY, null);

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

    /**
     * 시나리오 → 10 Panel 비동기 생성.
     * 2-12b에서 본체 구현 — ScenarioAnalyzer → 10 Panel PENDING save → 순차 ComfyUI 생성.
     */
    @Async
    @Transactional
    public void processPanelGeneration(Long taskId, Long episodeId,
                                       AiPanelsGenerateRequest request) {
        log.info("ComfyUIOrchestrator.processPanelGeneration stub — taskId={}, episodeId={}",
                taskId, episodeId);
    }
}
