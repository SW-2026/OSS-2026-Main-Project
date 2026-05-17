package com.wit.episode.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({ "episodeId", "epNumber", "epTitle", "panelCount", "createdAt" })
public class EpisodeSummaryResponse {
    private Long episodeId;
    private Long epNumber;
    private String epTitle;
    private int panelCount;
    private LocalDateTime createdAt;
}