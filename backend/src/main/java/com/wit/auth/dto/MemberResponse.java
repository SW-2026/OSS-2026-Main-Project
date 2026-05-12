package com.wit.auth.dto;

import com.wit.member.domain.Member;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberResponse {

    private Long memberId;    // 엔티티의 @Id 필드명과 일치시킴
    private String email;
    private String nickname;

    /**
     * 엔티티로부터 DTO를 생성하는 정적 팩토리 메서드
     */
    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .build();
    }
}