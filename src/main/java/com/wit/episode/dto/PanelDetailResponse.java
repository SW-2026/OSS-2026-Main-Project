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
    private final String layoutData; // 단건 조회 시 필요
    private final String prompt;     // 단건 조회 시 필요

    @Builder
    private PanelDetailResponse(Long panelId, int panelOrder, PanelStatus status,
                          String finalImageUrl, String layoutData, String prompt) {
        this.panelId = panelId;
        this.panelOrder = panelOrder;
        this.status = status;
        this.finalImageUrl = finalImageUrl;
        this.layoutData = layoutData;
        this.prompt = prompt;
    }

    // 엔티티를 DTO로 변환하는 정적 메서드
    public static PanelDetailResponse from(Panel panel) {
        return PanelDetailResponse.builder()
                .panelId(panel.getPanelId())
                .panelOrder(panel.getPanelOrder())
                .status(panel.getStatus())
                .finalImageUrl(panel.getFinalImageUrl())
                .layoutData(panel.getLayoutData())
                .prompt(panel.getPrompt())
                .build();
    }
}
