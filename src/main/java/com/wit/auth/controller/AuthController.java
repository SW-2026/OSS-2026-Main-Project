package com.wit.auth.controller;

import com.wit.auth.dto.*;
import com.wit.auth.service.AuthService;
import com.wit.member.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. 회원가입: 이메일/비번/닉네임
    @PostMapping("/register")
    public ResponseEntity<MemberResponse> register(@Valid @RequestBody RegisterRequest request) {
        Member savedMember = authService.register(request);

        return ResponseEntity.ok(MemberResponse.from(savedMember));
    }

    // 2. 로그인: JWT 토큰 반환
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // 3. 내 정보 조회: 토큰으로 본인 확인
    // SecurityContext에 저장된 유저 정보를 가져오기 위해 @AuthenticationPrincipal 사용
    @GetMapping("/me")
    public ResponseEntity<MemberDetailResponse> getMyInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long memberId = principalDetails.getMember().getMemberId();
        MemberDetailResponse response = authService.getMyInfo(memberId);
        return ResponseEntity.ok(response);
    }
}
