package com.wit.episode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EpisodeResponse {
    private Long episodeId;
    private Long projectId;
    private Long epNumber;
    private String epTitle;
    private LocalDateTime createdAt;
}
