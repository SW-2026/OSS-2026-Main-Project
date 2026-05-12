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
    @Column(name = "status", length = 30)
    private PanelStatus status;

    private String finalImageUrl;

    // === AI 이미지 생성 파이프라인용 내부 상태 필드 ===

    @Column(columnDefinition = "TEXT")
    private String scenarioText;

    @Column(columnDefinition = "TEXT")
    private String extractedParams;

    @Column(columnDefinition = "TEXT")
    private String layoutData; // 레이어별 위치, 크기 정보 (JSON 문자열)

    @Column(columnDefinition = "TEXT")
    private String finalPrompt;

    private Long seed;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Builder
    public Panel(int panelOrder, PanelStatus status, String finalImageUrl,
                 String scenarioText, String extractedParams, String finalPrompt,
                 Long seed, String errorMessage) {
        this.panelOrder = panelOrder;
        this.status = status;
        this.finalImageUrl = finalImageUrl;
        this.scenarioText = scenarioText;
        this.extractedParams = extractedParams;
        this.finalPrompt = finalPrompt;
        this.seed = seed;
        this.errorMessage = errorMessage;
    }

    // === 내부 파이프라인에서 사용하는 상태 갱신 메서드 (외부 API에 노출 X) ===

    public void updateScenario(String scenarioText) {
        this.scenarioText = scenarioText;
    }

    public void updateExtractedParams(String extractedParams) {
        this.extractedParams = extractedParams;
    }

    public void updateFinalPrompt(String finalPrompt) {
        this.finalPrompt = finalPrompt;
    }

    public void updateSeed(Long seed) {
        this.seed = seed;
    }

    public void markFailed(String errorMessage) {
        this.status = PanelStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    //컷 순서 변경
    public void updateOrder(int panelOrder) {
        this.panelOrder = panelOrder;
    }

    // 레이어 데이터 업데이트를 위한 비즈니스 메서드
    public void updateLayoutData(String layoutData) {
        this.layoutData = layoutData;
    }

    public void updateStatus(PanelStatus status) {
        this.status = status;
    }

    public void updateFinalImageUrl(String finalImageUrl) {
        this.finalImageUrl = finalImageUrl;
    }

    // 에러 메시지만 단독 갱신 — markFailed는 상태+에러를 함께 바꾸는 헬퍼이고, 이건 메시지만 갱신할 때 사용
    public void updateErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
