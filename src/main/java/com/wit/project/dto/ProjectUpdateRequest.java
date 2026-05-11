package com.wit.project.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 프로젝트 부분 수정용 DTO — 모든 필드 nullable, null이 아닌 필드만 덮어씀 (PATCH 의미)
@Getter
@NoArgsConstructor
public class ProjectUpdateRequest {

    @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
    private String title;

    @Size(max = 50, message = "장르는 50자 이내여야 합니다.")
    private String genre;

    // === AI 이미지 생성용 프롬프트 필드 ===

    private String styleBasePrompt;       // 공통 스타일 베이스 프롬프트 (TEXT)
    private String negativePrompt;        // 공통 부정 프롬프트 (TEXT)
    private String backgroundPrompt;      // 배경 처리 프롬프트 (TEXT)

    @Size(max = 200, message = "loraTriggerTag는 200자 이내여야 합니다.")
    private String loraTriggerTag;        // LoRA 호출 태그

    @Size(max = 500, message = "loraModelPath는 500자 이내여야 합니다.")
    private String loraModelPath;         // ComfyUI 서버에서 인식하는 LoRA 파일 경로/이름

    private String characterAppearancePrompt; // 캐릭터 외형 프롬프트 (TEXT)
    private String characterOutfitPrompt;     // 기본 의상 프롬프트 (TEXT)
}
