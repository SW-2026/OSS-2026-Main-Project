package com.wit.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 프로젝트 목록 조회용 요약 DTO — 회차 수만 포함하고 회차 상세는 제외
@Getter
@AllArgsConstructor
public class ProjectSummaryResponse {
    private Long projectId;
    private String title;
    private String genre;
    private int episodeCount; // 해당 프로젝트에 등록된 회차 수
    private LocalDateTime createdAt;
}
