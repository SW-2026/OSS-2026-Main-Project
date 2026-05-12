package com.wit.auth.service;

import com.wit.auth.dto.*;
import com.wit.auth.jwt.JwtTokenProvider;
import com.wit.member.domain.Member;
import com.wit.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 1. register: 회원가입
    public Member register(RegisterRequest request) { // void -> Member
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();

        // save()의 결과를 return해서 컨트롤러에 전달합니다.
        return memberRepository.save(member);
    }

    // 2. login: 로그인 요청 확인, jwt 토큰 반환
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        return new TokenResponse(jwtTokenProvider.generateToken(member.getMemberId()), "Bearer");
    }

    // 3. getMyInfo: 이메일을 통해 유저 정보 조회
    @Transactional(readOnly = true)
    public MemberDetailResponse getMyInfo(Long memberId) { // 파라미터 타입을 Long으로 변경
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return MemberDetailResponse.from(member);
    }
}
