package com.wit.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 프로젝트 상세 조회 DTO — 소속 회차의 간략 정보까지 함께 포함
@Getter
@AllArgsConstructor
public class ProjectDetailResponse {
    private Long projectId;
    private String title;
    private String genre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<EpisodeBrief> episodes; // 회차 요약 리스트 (없으면 빈 리스트)

    @Getter
    @AllArgsConstructor
    public static class EpisodeBrief {
        private Long episodeId;
        private Integer epNumber;
        private String epTitle;
    }
}
