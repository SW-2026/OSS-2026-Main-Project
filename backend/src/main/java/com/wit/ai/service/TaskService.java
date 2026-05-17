package com.wit.ai.service;

import com.wit.ai.domain.AiTask;
import com.wit.ai.dto.TaskResponse;
import com.wit.ai.repository.AiTaskRepository;
import com.wit.member.domain.Member;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final AiTaskRepository aiTaskRepository;

    public TaskResponse getTaskStatus(Member member, Long taskId) {
        AiTask task = aiTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 작업을 찾을 수 없습니다. ID: " + taskId));
        if (!task.getMember().getMemberId().equals(member.getMemberId())) {
            throw new AccessDeniedException("해당 작업에 대한 접근 권한이 없습니다.");
        }
        return toResponse(task);
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
