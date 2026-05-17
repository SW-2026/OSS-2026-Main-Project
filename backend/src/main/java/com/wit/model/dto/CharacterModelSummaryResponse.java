package com.wit.model.dto;

import com.wit.model.domain.ModelStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 모델 목록 응답 DTO — 경량 (목록 조회용)
@Getter
@AllArgsConstructor
public class CharacterModelSummaryResponse {
    private Long modelId;
    private String modelName;
    private ModelStatus status;
    private LocalDateTime createdAt;
}
