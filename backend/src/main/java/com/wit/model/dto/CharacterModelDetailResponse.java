package com.wit.model.dto;

import com.wit.model.domain.ModelStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 모델 상세 응답 DTO — 단건 조회 + 생성 응답에 사용
@Getter
@AllArgsConstructor
public class CharacterModelDetailResponse {
    private Long modelId;
    private Long projectId;
    private String modelName;
    private String triggerWord;
    private String loraModelPath;          // 학습 완료 후 채워짐, 미학습 시 null
    private String appearancePrompt;
    private String outfitPrompt;
    private ModelStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
