package com.wit.episode.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PanelStatus {
    PENDING("대기"),
    PROCESSING("생성 중"),
    COMPLETED("완료"),
    CREATED("빈 컷 생성 완료"),
    FAILED("실패");

    private final String description;
}
