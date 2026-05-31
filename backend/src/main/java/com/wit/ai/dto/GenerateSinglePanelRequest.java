package com.wit.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

// 1컷 생성 요청 — characterIds[]는 첫 번째 1명만 반영(컷당 1캐릭터), backgroundAssetId는 0~1개
public record GenerateSinglePanelRequest(
        @NotBlank String scenarioText,
        @Size(max = 20) List<Long> characterIds,
        Long backgroundAssetId
) {}
