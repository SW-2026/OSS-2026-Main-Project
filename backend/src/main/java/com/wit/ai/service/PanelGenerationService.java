package com.wit.ai.service;

import com.wit.ai.domain.AiTask;
import com.wit.ai.domain.TaskStatus;
import com.wit.ai.domain.TaskType;
import com.wit.ai.dto.AiPanelsGenerateRequest;
import com.wit.ai.dto.BackgroundMention;
import com.wit.ai.dto.CharacterMention;
import com.wit.ai.dto.GenerateSinglePanelRequest;
import com.wit.ai.dto.TaskResponse;
import com.wit.ai.repository.AiTaskRepository;
import com.wit.background_asset.domain.BackgroundAsset;
import com.wit.background_asset.repository.BackgroundAssetRepository;
import com.wit.episode.domain.Episode;
import com.wit.episode.repository.EpisodeRepository;
import com.wit.member.domain.Member;
import com.wit.model.domain.CharacterModel;
import com.wit.model.repository.CharacterModelRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class PanelGenerationService {

    private final EpisodeRepository episodeRepository;
    private final CharacterModelRepository characterModelRepository;
    private final BackgroundAssetRepository backgroundAssetRepository;
    private final AiTaskRepository aiTaskRepository;
    private final ComfyUIOrchestrator orchestrator;

    public TaskResponse generate(Member member, Long episodeId,
                                 AiPanelsGenerateRequest request) {
        Episode episode = validateEpisodeAccess(member, episodeId);
        Long projectId = episode.getProject().getProjectId();
        validateCharacterMentions(projectId, request.characters());
        validateBackgroundMentions(member, request.backgrounds());

        AiTask task = aiTaskRepository.save(
                AiTask.builder()
                        .member(member)
                        .taskType(TaskType.PANELS)
                        .status(TaskStatus.PENDING)
                        .progressPercent(0)
                        .targetType("Episode")
                        .targetId(episodeId)
                        .build()
        );

        Long taskId = task.getTaskId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            orchestrator.processPanelGeneration(taskId, episodeId, request);
                        }
                    });
        } else {
            orchestrator.processPanelGeneration(taskId, episodeId, request);
        }

        return toResponse(task);
    }

    // 1컷 생성 — generate()와 독립. 검증/AiTask/afterCommit 패턴 재사용, 첫 번째 캐릭터 1명만 반영
    public TaskResponse generateSingle(Member member, Long episodeId,
                                       GenerateSinglePanelRequest request) {
        Episode episode = validateEpisodeAccess(member, episodeId);
        Long projectId = episode.getProject().getProjectId();

        Long characterModelId = (request.characterIds() != null && !request.characterIds().isEmpty())
                ? request.characterIds().get(0) : null;
        if (characterModelId != null) {
            validateCharacterMentions(projectId,
                    List.of(new CharacterMention(null, characterModelId, null)));
        }
        if (request.backgroundAssetId() != null) {
            validateBackgroundMentions(member,
                    List.of(new BackgroundMention(null, request.backgroundAssetId())));
        }

        AiTask task = aiTaskRepository.save(
                AiTask.builder()
                        .member(member)
                        .taskType(TaskType.PANELS)
                        .status(TaskStatus.PENDING)
                        .progressPercent(0)
                        .targetType("Episode")
                        .targetId(episodeId)
                        .build()
        );

        Long taskId = task.getTaskId();
        Long charId = characterModelId;
        Long bgId = request.backgroundAssetId();
        String scenario = request.scenarioText();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            orchestrator.processSinglePanelGeneration(taskId, episodeId, charId, bgId, scenario);
                        }
                    });
        } else {
            orchestrator.processSinglePanelGeneration(taskId, episodeId, charId, bgId, scenario);
        }

        return toResponse(task);
    }

    private Episode validateEpisodeAccess(Member member, Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 에피소드를 찾을 수 없습니다. ID: " + episodeId));
        if (!episode.getProject().getMember().getMemberId().equals(member.getMemberId())) {
            throw new AccessDeniedException("해당 에피소드에 대한 접근 권한이 없습니다.");
        }
        return episode;
    }

    private void validateCharacterMentions(Long projectId, List<CharacterMention> mentions) {
        if (mentions == null || mentions.isEmpty()) {
            return;
        }
        List<Long> modelIds = mentions.stream()
                .map(CharacterMention::modelId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (modelIds.isEmpty()) {
            return;
        }
        List<CharacterModel> models = characterModelRepository.findAllById(modelIds);
        if (models.size() != modelIds.size()) {
            throw new EntityNotFoundException("일부 CharacterModel을 찾을 수 없습니다.");
        }
        for (CharacterModel m : models) {
            if (!m.getProject().getProjectId().equals(projectId)) {
                throw new AccessDeniedException(
                        "다른 프로젝트의 CharacterModel입니다. modelId=" + m.getModelId());
            }
        }
    }

    private void validateBackgroundMentions(Member member, List<BackgroundMention> backgrounds) {
        if (backgrounds == null || backgrounds.isEmpty()) {
            return;
        }
        List<Long> assetIds = backgrounds.stream()
                .map(BackgroundMention::assetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (assetIds.isEmpty()) {
            return;
        }
        List<BackgroundAsset> assets = backgroundAssetRepository.findAllById(assetIds);
        if (assets.size() != assetIds.size()) {
            throw new EntityNotFoundException("일부 BackgroundAsset을 찾을 수 없습니다.");
        }
        for (BackgroundAsset asset : assets) {
            Member owner = asset.getMember();
            if (owner == null) {
                throw new EntityNotFoundException(
                        "데이터 무결성 오류 — BackgroundAsset의 member가 없음. assetId=" + asset.getAssetId());
            }
            if (!owner.getMemberId().equals(member.getMemberId())) {
                throw new AccessDeniedException(
                        "다른 사용자의 BackgroundAsset입니다. assetId=" + asset.getAssetId());
            }
        }
    }

    private TaskResponse toResponse(AiTask task) {
        return new TaskResponse(
                task.getTaskId(),
                task.getTaskType(),
                task.getStatus(),
                task.getProgressPercent(),
                task.getTargetType(),
                task.getTargetId(),
                task.getResultUrl(),
                task.getErrorMessage()
        );
    }
}
