package com.wit.ai.controller;

import com.wit.auth.dto.PrincipalDetails;
import com.wit.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 본 단계 (2-15) — segment / refine / compose 명세 준수용 mock 컨트롤러.
 * 본격 구현은 별도 트랙 (제안서 담당자):
 * - segment → 유수빈 (생성 이미지 레이어 분리)
 * - refine  → 김서영/박준희 (이미지 벡터 변환 및 선화 편집)
 * - compose → 이형석 (배경·캐릭터 병합, denoise 워크플로우는 3단계)
 *
 * 본 단계 mock 정책: DB 변경 없음. ApiResponse 200 + Map body. 인증만 적용.
 */
@RestController
@RequiredArgsConstructor
public class AiMockController {

    record SegmentRequest(Long panelId) {}
    record RefineRequest(Long panelId, String layerType, String newPrompt) {}
    record ComposeRequest(Long characterAssetId, Long backgroundAssetId,
                          Map<String, Object> position, Double denoise) {}

    @PostMapping("/api/ai/segment")
    public ApiResponse<Map<String, Object>> segment(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody SegmentRequest request) {
        return ApiResponse.ok(Map.of(
                "characterUrl", "/images/panel/placeholder.png",
                "backgroundUrl", "/images/panel/placeholder.png",
                "message", "본 기능은 추후 구현됩니다."
        ));
    }

    @PostMapping("/api/ai/refine")
    public ApiResponse<Map<String, Object>> refine(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody RefineRequest request) {
        return ApiResponse.ok(Map.of(
                "message", "본 기능은 추후 구현됩니다.",
                "echo", Map.of(
                        "panelId", request.panelId() == null ? -1L : request.panelId(),
                        "layerType", request.layerType() == null ? "" : request.layerType(),
                        "newPrompt", request.newPrompt() == null ? "" : request.newPrompt()
                )
        ));
    }

    @PostMapping("/api/ai/compose")
    public ApiResponse<Map<String, Object>> compose(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody ComposeRequest request) {
        return ApiResponse.ok(Map.of(
                "message", "본 기능은 추후 구현됩니다.",
                "echo", Map.of(
                        "characterAssetId", request.characterAssetId() == null ? -1L : request.characterAssetId(),
                        "backgroundAssetId", request.backgroundAssetId() == null ? -1L : request.backgroundAssetId()
                )
        ));
    }
}
