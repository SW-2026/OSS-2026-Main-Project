package com.wit.episode.dto;

import com.wit.episode.domain.Panel;
import com.wit.episode.domain.PanelStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PanelDetailResponse {

    private final Long panelId;
    private final int panelOrder;
    private final PanelStatus status;
    private final String finalImageUrl;
    private final String layoutData; // 단건 조회 시 필요, 캔버스 복구용 JSON
    private final String prompt;     // 단건 조회 시 필요
    private final Long characterAssetId;
    private final String characterAssetUrl;
    private final Long backgroundAssetId;
    private final String backgroundAssetUrl;

    @Builder
    private PanelDetailResponse(Long panelId, int panelOrder, PanelStatus status,
                                String finalImageUrl, String layoutData, String prompt,
                                Long characterAssetId, String characterAssetUrl,
                                Long backgroundAssetId, String backgroundAssetUrl) {
        this.panelId = panelId;
        this.panelOrder = panelOrder;
        this.status = status;
        this.finalImageUrl = finalImageUrl;
        this.layoutData = layoutData;
        this.prompt = prompt;
        this.characterAssetId = characterAssetId;
        this.characterAssetUrl = characterAssetUrl;
        this.backgroundAssetId = backgroundAssetId;
        this.backgroundAssetUrl = backgroundAssetUrl;
    }

    // 엔티티를 DTO로 변환 — URL 없는 경로 (호환용, ID는 Panel에서 추출)
    public static PanelDetailResponse from(Panel panel) {
        return from(panel, null, null);
    }

    // overload — Asset URL 포함 변환 (PanelService.getPanelsWithAssets에서 사용)
    public static PanelDetailResponse from(Panel panel,
                                           String characterAssetUrl,
                                           String backgroundAssetUrl) {
        return PanelDetailResponse.builder()
                .panelId(panel.getPanelId())
                .panelOrder(panel.getPanelOrder())
                .status(panel.getStatus())
                .finalImageUrl(panel.getFinalImageUrl())
                .layoutData(panel.getLayoutData())
                .prompt(panel.getFinalPrompt())
                .characterAssetId(panel.getCharacterAssetId())
                .characterAssetUrl(characterAssetUrl)
                .backgroundAssetId(panel.getBackgroundAssetId())
                .backgroundAssetUrl(backgroundAssetUrl)
                .build();
    }
}
