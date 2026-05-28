package com.wit.episode.domain;

import com.wit.model.domain.CharacterModel;
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
    @JoinColumn(name = "episode_id", nullable = false)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_model_id", nullable = true)
    private CharacterModel characterModel;

    @Column(name = "character_asset_id")
    private Long characterAssetId;

    @Column(name = "background_asset_id")
    private Long backgroundAssetId;

    @Column(columnDefinition = "TEXT")
    private String layoutData; // 레이어별 위치, 크기 정보 (JSON 문자열)

    @Column(columnDefinition = "LONGTEXT")
    private String cutEditorData; // 편집기 그림 데이터 (strokes/balloons/canvasImages/layers JSON 문자열)

    @Column(columnDefinition = "TEXT")
    private String finalPrompt;

    private Long seed;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Builder
    public Panel(int panelOrder, PanelStatus status, String finalImageUrl,
                 String scenarioText, String extractedParams, String finalPrompt,
                 Long seed, String errorMessage,
                 Long characterAssetId, Long backgroundAssetId) {
        this.panelOrder = panelOrder;
        this.status = status;
        this.finalImageUrl = finalImageUrl;
        this.scenarioText = scenarioText;
        this.extractedParams = extractedParams;
        this.finalPrompt = finalPrompt;
        this.seed = seed;
        this.errorMessage = errorMessage;
        this.characterAssetId = characterAssetId;
        this.backgroundAssetId = backgroundAssetId;
    }

    // === 내부 파이프라인에서 사용하는 상태 갱신 메서드 (외부 API에 노출 X) ===

    public void updateScenario(String scenarioText) {
        this.scenarioText = scenarioText;
    }

    public void updateExtractedParams(String extractedParams) {
        this.extractedParams = extractedParams;
    }

    public void updateCharacterModel(CharacterModel characterModel) {
        this.characterModel = characterModel;
    }

    public void updateCharacterAssetId(Long characterAssetId) {
        this.characterAssetId = characterAssetId;
    }

    public void updateBackgroundAssetId(Long backgroundAssetId) {
        this.backgroundAssetId = backgroundAssetId;
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

    // 편집기 그림 데이터 (strokes/balloons/canvasImages/layers) 업데이트
    public void updateCutEditorData(String cutEditorData) {
        this.cutEditorData = cutEditorData;
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
