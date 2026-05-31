package com.wit.episode.controller;

import com.wit.ai.dto.AiPanelsGenerateRequest;
import com.wit.ai.dto.GenerateSinglePanelRequest;
import com.wit.ai.dto.TaskResponse;
import com.wit.ai.service.PanelGenerationService;
import com.wit.auth.dto.PrincipalDetails;
import com.wit.episode.dto.CutEditorDataRequest;
import com.wit.episode.dto.CutEditorDataResponse;
import com.wit.episode.dto.PanelDetailResponse;
import com.wit.episode.dto.PanelReorderRequest;
import com.wit.episode.service.PanelService;
import com.wit.global.response.ApiResponse;
import com.wit.member.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PanelController {

    private final PanelService panelService;
    private final PanelGenerationService panelGenerationService;

    // 시나리오 바탕으로 컷 자동 생성 (비동기 → 202 Accepted + taskId 반환)
    @PostMapping("/episodes/{episodeId}/panels/generate")
    public ResponseEntity<ApiResponse<TaskResponse>> generatePanels(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("episodeId") Long episodeId,
            @Valid @RequestBody AiPanelsGenerateRequest request) {

        Member member = principalDetails.getMember();
        TaskResponse response =
                panelGenerationService.generate(member, episodeId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(response));
    }

    // 1컷 자동 생성 (비동기 → 202 + taskId). 폴링은 기존 /api/ai/tasks/{id} 재사용
    @PostMapping("/episodes/{episodeId}/panels/generate-single")
    public ResponseEntity<ApiResponse<TaskResponse>> generateSinglePanel(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("episodeId") Long episodeId,
            @Valid @RequestBody GenerateSinglePanelRequest request) {

        Member member = principalDetails.getMember();
        TaskResponse response =
                panelGenerationService.generateSingle(member, episodeId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(response));
    }

    // 컷 목록 조회 (Phase 1: 캐릭터/배경 Asset URL 포함)
    @GetMapping("/episodes/{episodeId}/panels")
    public ApiResponse<List<PanelDetailResponse>> getPanels(@PathVariable("episodeId") Long episodeId) {
        return ApiResponse.ok(panelService.getPanelsWithAssets(episodeId));
    }

    //컷 단건 조회 (Phase 1: 캐릭터/배경 Asset URL 포함)
    @GetMapping("/panels/{panelId}")
    public ApiResponse<PanelDetailResponse> getPanel(@PathVariable("panelId") Long panelId) {
        return ApiResponse.ok(panelService.getPanelWithAssets(panelId));
    }

    // 컷 수동 생성
    @PostMapping("/episodes/{episodeId}/panels")
    public ApiResponse<Long> createPanel(@PathVariable("episodeId") Long episodeId) {
        Long newPanelID = panelService.createPanel(episodeId);
        return ApiResponse.ok(newPanelID);
    }

    //컷 순서 변경
    @PatchMapping("/episodes/{episodeId}/panels/reorder")
    public ApiResponse<Void> reorderPanels(
            @PathVariable("episodeId") Long episodeId,
            @RequestBody PanelReorderRequest request) {
        panelService.reorderPanels(episodeId, request.getPanelIds());
        return ApiResponse.ok(null);
    }

    // 컷 삭제
    @DeleteMapping("/panels/{panelId}")
    public ApiResponse<Void> deletePanel(@PathVariable("panelId") Long panelId) {
        panelService.deletePanel(panelId);
        return ApiResponse.ok(null);
    }

    // 컷 편집기 데이터(strokes/balloons/canvasImages/layers JSON) 저장 — frontend supabase 대체
    @PatchMapping("/panels/{panelId}/cut-data")
    public ApiResponse<Void> saveCutEditorData(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("panelId") Long panelId,
            @Valid @RequestBody CutEditorDataRequest request) {
        Member member = principalDetails.getMember();
        panelService.saveCutEditorData(member, panelId, request.cutEditorData());
        return ApiResponse.ok(null);
    }

    // 컷 편집기 데이터 조회 (panel.cutEditorData가 NULL이면 응답.data.cutEditorData=null)
    @GetMapping("/panels/{panelId}/cut-data")
    public ApiResponse<CutEditorDataResponse> getCutEditorData(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("panelId") Long panelId) {
        Member member = principalDetails.getMember();
        return ApiResponse.ok(panelService.getCutEditorData(member, panelId));
    }
}
