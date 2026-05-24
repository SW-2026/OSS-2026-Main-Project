package com.wit.episode.controller;

import com.wit.global.response.ApiResponse;
import com.wit.episode.dto.*;
import com.wit.episode.service.EpisodeService;
import com.wit.member.domain.Member;
import com.wit.auth.dto.PrincipalDetails; // 사용자 정의 어노테이션 혹은 Security 설정에 따라 다름
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EpisodeController {

    private final EpisodeService episodeService;

    /**
     * 1. 에피소드 생성 (POST)
     * 요청 예시: { "epNumber": 1, "epTitle": "1화 - 운명의 만남" }
     */
    @PostMapping("/api/projects/{projectId}/episodes")
    public ApiResponse<EpisodeResponse> create(
            @AuthenticationPrincipal PrincipalDetails principalDetails, // 현재 로그인한 사용자 정보
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody EpisodeCreateRequest request
    ) {
        Member member = principalDetails.getMember();

        EpisodeResponse response = episodeService.create(member, projectId, request);
        return ApiResponse.created(response); // 201 Created 응답
    }

    /**
     * 2. 프로젝트 내 전체 에피소드 목록 조회 (GET)
     */
    @GetMapping("/api/projects/{projectId}/episodes")
    public ApiResponse<List<EpisodeSummaryResponse>> getAll(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("projectId") Long projectId
    ) {
        Member member = principalDetails.getMember();

        List<EpisodeSummaryResponse> responses = episodeService.getAll(member, projectId);
        return ApiResponse.ok(responses); // 200 OK 응답
    }

    /**
     * 3. 특정 에피소드 상세 조회 (GET)
     * 상세 정보와 패널 리스트를 함께 반환합니다.
     */
    @GetMapping("/api/episodes/{episodeId}")
    public ApiResponse<EpisodeDetailResponse> getOne(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            //@PathVariable Long projectId, // 경로는 유지하되 로직상 필요한 경우 사용
            @PathVariable("episodeId") Long episodeId
    ) {
        Member member = principalDetails.getMember();

        EpisodeDetailResponse response = episodeService.getOne(member, episodeId);
        return ApiResponse.ok(response);
    }

    /**
     * 4. 에피소드 부분 수정 (PATCH /api/episodes/{episodeId})
     * 요청 바디는 EpisodeUpdateRequest — null이 아닌 필드만 덮어씁니다.
     */
    @PatchMapping("/api/episodes/{episodeId}")
    public ApiResponse<EpisodeResponse> update(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("episodeId") Long episodeId,
            @Valid @RequestBody EpisodeUpdateRequest request
    ) {
        Member member = principalDetails.getMember();
        EpisodeResponse response = episodeService.update(member, episodeId, request);
        return ApiResponse.ok(response);
    }

    /**
     * 5. 에피소드 삭제 (DELETE /api/episodes/{episodeId})
     * 소유자 검증 후 에피소드와 하위 패널을 cascade로 함께 삭제합니다.
     */
    @DeleteMapping("/api/episodes/{episodeId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("episodeId") Long episodeId
    ) {
        Member member = principalDetails.getMember();
        episodeService.delete(member, episodeId);
        return ApiResponse.<Void>ok(null);
    }
}
