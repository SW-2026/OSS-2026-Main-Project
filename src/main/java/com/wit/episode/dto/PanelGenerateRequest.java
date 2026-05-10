package com.wit.episode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PanelGenerateRequest {
    private String scenario; // 핵심 시나리오 텍스트
    private String modelId;  // 선택한 AI 모델 ID
}
