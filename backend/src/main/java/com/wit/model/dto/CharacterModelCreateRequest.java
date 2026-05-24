package com.wit.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 모델 생성 요청 DTO — modelName 필수, 나머지 nullable (학습 전에는 정보 없을 수 있음)
@Getter
@NoArgsConstructor
public class CharacterModelCreateRequest {

    @NotBlank(message = "모델명은 필수입니다.")
    @Size(max = 100, message = "모델명은 100자 이내여야 합니다.")
    private String modelName;

    @Size(max = 100, message = "triggerWord는 100자 이내여야 합니다.")
    private String triggerWord;            // LoRA 트리거 (예: "anya_v1"), 학습 후 확정 가능

    @Size(max = 500, message = "loraModelPath는 500자 이내여야 합니다.")
    private String loraModelPath;          // LoRA 산출물 파일명 (확장자 제외, 예: "anya_v1")

    private String appearancePrompt;       // 캐릭터 외형 프롬프트 (TEXT)

    private String outfitPrompt;           // 기본 의상 프롬프트 (TEXT)
}
