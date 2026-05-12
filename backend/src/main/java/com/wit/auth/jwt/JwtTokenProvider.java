package com.wit.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Base64;

@Slf4j
@Component
public class JwtTokenProvider {

    private final String secretKeyPlain;
    private final long expiration;
    private Key key; // 보안을 위해 Key 객체로 관리

    public JwtTokenProvider(
            @Value("${jwt.secret}")  String secretKey,
            @Value("${jwt.expiration}") long expiration) {
        this.secretKeyPlain = secretKey;
        this.expiration = expiration;
    }

    // 객체 생성 후 주입된 secretKey 문자열을 Key 객체로 초기화
    @PostConstruct
    protected void init() {
        // 1. 키가 너무 짧으면 어차피 에러가 나므로 미리 체크
        if (secretKeyPlain == null || secretKeyPlain.length() < 32) {
            throw new IllegalArgumentException("JWT 시크릿 키는 최소 32자 이상이어야 합니다. 현재 길이: " +
                    (secretKeyPlain != null ? secretKeyPlain.length() : 0));
        }

        // 2. 표준 Base64 인코딩을 거쳐 안전하게 키 객체 생성
        String encodedKey = Base64.getEncoder().encodeToString(secretKeyPlain.getBytes(StandardCharsets.UTF_8));
        this.key = Keys.hmacShaKeyFor(encodedKey.getBytes(StandardCharsets.UTF_8));

        System.out.println("✅ JWT Token Provider 초기화 완료!");
    }

    /**
     * 토큰 생성
     * AuthService에서 호출할 때 member.getMemberId()를 인자로 받습니다.
     */
    public String generateToken(Long memberId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(key, SignatureAlgorithm.HS256) // Key 객체 사용
                .compact();
    }

    /**
     * 토큰에서 MemberId 추출
     */
    public Long getMemberId(String token) {
        return Long.parseLong(
                Jwts.parserBuilder() // 최신 버전에서 권장하는 parserBuilder
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject()
        );
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}