package com.wit.episode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor

public class EpisodeSummaryResponse {
    private Long episodeId;
    private Long epNumber;
    private String epTitle;
    private int panelCount;
    private LocalDateTime createdAt;
}