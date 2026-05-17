package com.wit.model.dto;

import com.wit.model.domain.ModelStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 학습 트리거 응답 DTO — modelId + 현재 상태
@Getter
@AllArgsConstructor
public class CharacterModelStatusResponse {
    private Long modelId;
    private ModelStatus status;
}
