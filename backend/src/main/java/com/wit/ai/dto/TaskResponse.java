package com.wit.ai.dto;

import com.wit.ai.domain.TaskStatus;
import com.wit.ai.domain.TaskType;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 비동기 작업 polling 응답 DTO — GET /api/ai/tasks/{taskId}
@Getter
@AllArgsConstructor
public class TaskResponse {
    private Long taskId;
    private TaskType taskType;
    private TaskStatus status;
    private Integer progressPercent;
    private String targetType;
    private Long targetId;
    private String resultUrl;
    private String errorMessage;
}
