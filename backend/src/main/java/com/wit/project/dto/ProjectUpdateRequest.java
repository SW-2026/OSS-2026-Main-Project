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
}
