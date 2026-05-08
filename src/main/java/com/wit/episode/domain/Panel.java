package com.wit.episode.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Panel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long panelId;

    @Setter // 편의 메서드를 위해 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episodeId", nullable = false)
    private Episode episode;

    private int panelOrder; // 컷 순서

    @Enumerated(EnumType.STRING) // Enum을 문자열로 DB에 저장
    private PanelStatus status;

    private String finalImageUrl;

    @Builder
    public Panel(int panelOrder, PanelStatus status, String finalImageUrl) {
        this.panelOrder = panelOrder;
        this.status = status;
        this.finalImageUrl = finalImageUrl;
    }
}
