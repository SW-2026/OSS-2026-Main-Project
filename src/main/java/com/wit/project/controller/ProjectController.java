package com.wit.project.controller;

import com.wit.auth.dto.PrincipalDetails;
import com.wit.global.response.ApiResponse;
import com.wit.member.domain.Member;
import com.wit.project.dto.ProjectCreateRequest;
import com.wit.project.dto.ProjectDetailResponse;
import com.wit.project.dto.ProjectResponse;
import com.wit.project.dto.ProjectSummaryResponse;
import com.wit.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 1. 프로젝트 생성 (POST /api/projects)
     * 요청 예시: { "title": "내 첫 웹툰", "genre": "로맨스" }
     */
    @PostMapping
    public ApiResponse<ProjectResponse> create(
            @AuthenticationPrincipal PrincipalDetails principalDetails, // 현재 로그인한 사용자 정보
            @Valid @RequestBody ProjectCreateRequest request
    ) {
        Member member = principalDetails.getMember();
        ProjectResponse response = projectService.create(member, request);
        return ApiResponse.created(response); // 201 Created 응답
    }

    /**
     * 2. 내 프로젝트 목록 조회 (GET /api/projects)
     * 본인이 소유한 프로젝트만 반환합니다.
     */
    @GetMapping
    public ApiResponse<List<ProjectSummaryResponse>> getMyProjects(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.getMember();
        List<ProjectSummaryResponse> responses = projectService.getMyProjects(member);
        return ApiResponse.ok(responses); // 200 OK 응답
    }

    /**
     * 3. 프로젝트 상세 조회 (GET /api/projects/{id})
     * 소유자 검증 후 프로젝트 정보와 회차 요약 리스트를 반환합니다.
     */
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> getOne(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long projectId
    ) {
        Member member = principalDetails.getMember();
        ProjectDetailResponse response = projectService.getOne(member, projectId);
        return ApiResponse.ok(response);
    }

    /**
     * 4. 프로젝트 삭제 (DELETE /api/projects/{id})
     * 소유자 검증 후 프로젝트와 하위 회차/패널을 cascade로 함께 삭제합니다.
     */
    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long projectId
    ) {
        Member member = principalDetails.getMember();
        projectService.delete(member, projectId);
        return ApiResponse.<Void>ok(null); // 200 OK + data:null
    }
}
