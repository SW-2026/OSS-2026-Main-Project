package com.wit.episode.controller;

import com.wit.episode.domain.Panel;
import com.wit.episode.dto.PanelDetailResponse;
import com.wit.episode.dto.PanelGenerateRequest;
import com.wit.episode.dto.PanelReorderRequest;
import com.wit.episode.service.PanelService;
import com.wit.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PanelController {

    private final PanelService panelService;

    // 시나리오 바탕으로 컷 자동 생성
    @PostMapping("/episodes/{episodeId}/panels/generate")
    public ApiResponse<String> generatePanels(
            @PathVariable("episodeId") Long episodeId,
            @RequestBody PanelGenerateRequest request) {

        panelService.generatePanels(episodeId, request.getScenario());
        return ApiResponse.ok("시나리오 분석 및 이미지 생성이 시작되었습니다.");
    }

    // 컷 목록 조회
    @GetMapping("/episodes/{episodeId}/panels")
    public ApiResponse<List<PanelDetailResponse>> getPanels(@PathVariable("episodeId") Long episodeId) {
        List<PanelDetailResponse> responses = panelService.getPanels(episodeId).stream()
                .map(PanelDetailResponse::from)
                .collect(Collectors.toList());
        return ApiResponse.ok(responses);
    }

    //컷 단건 조회
    @GetMapping("/panels/{panelId}")
    public ApiResponse<PanelDetailResponse> getPanel(@PathVariable("panelId") Long panelId) {
        Panel panel = panelService.getPanel(panelId);
        return ApiResponse.ok(PanelDetailResponse.from(panel));
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
}
