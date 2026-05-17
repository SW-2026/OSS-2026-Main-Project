package com.wit.ai.service;

import com.wit.ai.domain.AiTask;
import com.wit.ai.domain.TaskStatus;
import com.wit.ai.domain.TaskType;
import com.wit.ai.dto.CharacterAssetGenerateRequest;
import com.wit.ai.dto.TaskResponse;
import com.wit.ai.repository.AiTaskRepository;
import com.wit.member.domain.Member;
import com.wit.model.domain.CharacterModel;
import com.wit.model.repository.CharacterModelRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CharacterGenerationService {

    private final CharacterModelRepository characterModelRepository;
    private final AiTaskRepository aiTaskRepository;
    private final ComfyUIOrchestrator orchestrator;

    public TaskResponse generate(Member member, Long projectId, Long modelId,
                                 CharacterAssetGenerateRequest request) {
        CharacterModel model = validateModelAccess(member, projectId, modelId);

        AiTask task = aiTaskRepository.save(
                AiTask.builder()
                        .member(member)
                        .taskType(TaskType.CHARACTER)
                        .status(TaskStatus.PENDING)
                        .progressPercent(0)
                        .build()
        );

        orchestrator.processCharacterGeneration(
                task.getTaskId(), model.getModelId(),
                request.poseTags(), request.backgroundTags());

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

    private CharacterModel validateModelAccess(Member member, Long projectId, Long modelId) {
        CharacterModel model = characterModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 모델을 찾을 수 없습니다. ID: " + modelId));
        if (!model.getProject().getProjectId().equals(projectId)) {
            throw new EntityNotFoundException(
                    "프로젝트와 모델이 매칭되지 않습니다. projectId=" + projectId
                            + ", modelId=" + modelId);
        }
        if (!model.getProject().getMember().getMemberId().equals(member.getMemberId())) {
            throw new AccessDeniedException("해당 모델에 대한 접근 권한이 없습니다.");
        }
        return model;
    }
}
