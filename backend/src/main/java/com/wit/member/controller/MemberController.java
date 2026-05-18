package com.wit.member.controller;

import com.wit.global.response.ApiResponse;
import com.wit.member.service.MemberService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 회원가입 API
    @PostMapping("/register")
    public ApiResponse<Long> register(@RequestBody RegisterRequest request) {
        Long memberId = memberService.join(
                request.getEmail(),
                request.getPassword(),
                request.getNickname()
        );
        return ApiResponse.created(memberId);
    }

    @Getter
    @NoArgsConstructor
    public static class RegisterRequest {
        private String email;
        private String password;
        private String nickname;
    }
}