package com.wit.episode.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 에피소드 부분 수정용 DTO — 모든 필드 nullable, null이 아닌 필드만 덮어씀 (PATCH 의미)
// Project의 ProjectUpdateRequest 패턴 그대로
@Getter
@NoArgsConstructor
public class EpisodeUpdateRequest {

    private Long epNumber;     // 회차 번호 (nullable, null이면 변경 안 함)

    @Size(max = 200, message = "회차 제목은 200자 이내여야 합니다.")
    private String epTitle;    // 회차 제목 (nullable, null이면 변경 안 함)
}
