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

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    private int panelOrder;

    @Enumerated(EnumType.STRING)
    private PanelStatus status;

    private String finalImageUrl; // 최종 병합 이미지

    @Column(columnDefinition = "TEXT")
    private String layoutData; // 레이어별 위치, 크기 정보 (JSON 문자열)

    @Column(columnDefinition = "TEXT")
    private String prompt; // AI 생성을 위한 프롬프트 혹은 시나리오 텍스트

    @Builder
    public Panel(int panelOrder, PanelStatus status, String finalImageUrl, String layoutData, String prompt) {
        this.panelOrder = panelOrder;
        this.status = status;
        this.finalImageUrl = finalImageUrl;
        this.layoutData = layoutData;
        this.prompt = prompt;
    }

    public void updateResult(String finalImageUrl, PanelStatus status) {
        this.finalImageUrl = finalImageUrl;
        this.status = status;
    }

    public void updateOrder(int newOrder) {
        this.panelOrder = newOrder;
    }
}