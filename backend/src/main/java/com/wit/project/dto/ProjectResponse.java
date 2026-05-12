package com.wit.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 프로젝트 생성 응답 DTO — 생성 직후 클라이언트에 돌려줄 기본 정보
@Getter
@AllArgsConstructor
public class ProjectResponse {
    private Long projectId;
    private Long memberId;
    private String title;
    private String genre;
    private LocalDateTime createdAt;
}
