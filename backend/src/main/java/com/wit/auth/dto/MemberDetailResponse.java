package com.wit.auth.dto;

import com.wit.member.domain.Member;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDetailResponse {

    private Long memberId;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;

    // 회원 정보 조회에서 사용
    public static MemberDetailResponse from(Member member) {
        return MemberDetailResponse.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
