package com.wit.background_asset.domain;

import com.wit.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BackgroundAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String assetName;
    private String assetUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public BackgroundAsset(Member member, String assetName, String assetUrl) {
        this.member = member;
        this.assetName = assetName;
        this.assetUrl = assetUrl;
    }
}