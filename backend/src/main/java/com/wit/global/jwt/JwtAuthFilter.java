package com.wit.global.jwt;

import com.wit.auth.dto.PrincipalDetails;
import com.wit.auth.jwt.JwtTokenProvider;
import com.wit.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 헤더에서 토큰 추출
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // 2. 토큰 유효성 검사
            if (jwtTokenProvider.validate(token)) {
                Long memberId = jwtTokenProvider.getMemberId(token);

                // 3. DB에서 사용자 조회 및 인증 객체 생성
                memberRepository.findById(memberId).ifPresent(member -> {
                    // 사용자님이 만드신 PrincipalDetails 활용
                    PrincipalDetails principalDetails = new PrincipalDetails(member);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principalDetails, // 단순 member 대신 상세 정보가 담긴 객체 사용
                                    null,
                                    principalDetails.getAuthorities()
                            );

                    // 4. 추가 보안 정보 설정 (IP, 세션 ID 등)
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5. SecurityContext에 인증 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("인증 성공: 사용자 ID {}", memberId);
                });
            }
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}