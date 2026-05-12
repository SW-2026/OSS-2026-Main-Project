package com.wit.episode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EpisodeCreateRequest {
    @NotNull(message = "회차 번호는 필수입니다.") // null 방지
    private Long epNumber;

    @NotBlank(message = "회차 제목을 입력해주세요.") // 빈 문자열("")이나 공백(" ") 방지
    private String epTitle;
}
