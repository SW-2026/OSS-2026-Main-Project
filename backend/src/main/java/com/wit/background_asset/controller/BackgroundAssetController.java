package com.wit.background_asset.controller;

import com.wit.background_asset.dto.BackgroundAssetRequest;
import com.wit.background_asset.dto.BackgroundAssetResponse;
import com.wit.background_asset.service.BackgroundAssetService;
import com.wit.global.response.ApiResponse;
import com.wit.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/background-assets")
@RequiredArgsConstructor
public class BackgroundAssetController {
    private final BackgroundAssetService assetService;

    @PostMapping
    public ApiResponse<Long> uploadAsset(
            @AuthenticationPrincipal Member member,
            @RequestBody BackgroundAssetRequest dto) {
        return ApiResponse.created(assetService.upload(member, dto));
    }

    @GetMapping
    public ApiResponse<List<BackgroundAssetResponse>> getMyAssets(
            @AuthenticationPrincipal Member member) {
        return ApiResponse.ok(assetService.getMyAssets(member));
    }

    @DeleteMapping("/{assetId}")
    public ApiResponse<Void> deleteAsset(
            @AuthenticationPrincipal Member member,
            @PathVariable Long assetId) {
        assetService.delete(member, assetId);
        return ApiResponse.ok(null);
    }
}