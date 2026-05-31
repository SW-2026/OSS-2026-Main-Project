package com.wit.global.controller;

import com.wit.auth.dto.PrincipalDetails;
import com.wit.global.config.AdminProperties;
import com.wit.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminProperties adminProperties;

    // 현재 로그인 사용자가 관리자인지 — 프론트 관리자 UI 노출 판단용
    @GetMapping("/check")
    public ApiResponse<Boolean> check(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        boolean isAdmin = adminProperties.isAdmin(principalDetails.getMember().getEmail());
        return ApiResponse.ok(isAdmin);
    }
}
