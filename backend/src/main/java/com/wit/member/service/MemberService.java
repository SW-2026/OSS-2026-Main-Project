package com.wit.member.service;

import com.wit.member.domain.Member;
import com.wit.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 가입 로직
    public Long join(String email, String password, String nickname) {
        // 중복 회원 검증 로직이 여기에 들어가면 좋겠죠?

        // 비밀번호는 반드시 암호화해서 저장해야 합니다.
        String encodedPassword = passwordEncoder.encode(password);

        Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .build();

        return memberRepository.save(member).getMemberId();
    }
}