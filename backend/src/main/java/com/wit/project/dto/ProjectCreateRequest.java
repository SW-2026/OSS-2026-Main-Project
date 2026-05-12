package com.wit.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectCreateRequest {
    @NotBlank(message = "프로젝트 제목을 입력해주세요.") // 빈 문자열("")이나 공백(" ") 방지
    @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
    private String title;

    // 장르는 선택값 — 입력하지 않으면 null로 저장
    @Size(max = 50, message = "장르는 50자 이내여야 합니다.")
    private String genre;
}
