package com.wit.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 캐릭터 단독 생성 결과 응답 DTO
@Getter
@AllArgsConstructor
public class CharacterAssetResponse {
    private Long assetId;
    private Long modelId;
    private Long projectId;
    private String imageUrl;
    private String finalPrompt;
    private Long seed;
    private LocalDateTime createdAt;
}
