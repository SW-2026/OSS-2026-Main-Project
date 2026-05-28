package com.wit.lora.controller;

import com.wit.auth.dto.PrincipalDetails;
import com.wit.global.response.ApiResponse;
import com.wit.lora.dto.LoraCatalogResponse;
import com.wit.lora.service.LoraCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LoraCatalogController {

    private final LoraCatalogService loraCatalogService;

    // 시스템 카탈로그 — 인증된 사용자 누구나 (Member 단위 제한 없음)
    @GetMapping("/api/loras")
    public ApiResponse<List<LoraCatalogResponse>> listLoras(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return ApiResponse.ok(loraCatalogService.findAll());
    }
}
