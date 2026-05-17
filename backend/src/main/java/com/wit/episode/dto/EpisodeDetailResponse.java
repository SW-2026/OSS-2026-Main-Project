package com.wit.episode.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.wit.episode.domain.PanelStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({ "episodeId", "epNumber", "epTitle", "panels" })
public class EpisodeDetailResponse {
    private Long episodeId;
    private Long epNumber;
    private String epTitle;
    private List<PanelResponse> panels; // 컷별 상세 정보 리스트

    @Getter
    @AllArgsConstructor
    @JsonPropertyOrder({ "panelId", "panelOrder", "status", "finalImageUrl" })
    public static class PanelResponse {
        private Long panelId;
        private int panelOrder;
        private PanelStatus status;        // 예: PENDING, COMPLETED
        private String finalImageUrl; // 완성된 이미지 URL (없으면 null)
    }
}

