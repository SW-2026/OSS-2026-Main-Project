package com.wit.auth.service;

import com.wit.auth.dto.PrincipalDetails;
import com.wit.member.domain.Member;
import lombok.RequiredArgsConstructor;
import com.wit.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public PrincipalDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다: " + email));

        return new PrincipalDetails(member); // 찾은 멤버를 PrincipalDetails로 감싸서 반환
    }
}