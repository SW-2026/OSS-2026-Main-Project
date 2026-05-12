package com.wit.episode.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({ "episodeId", "projectID", "epNumber", "epTitle", "createdAt" })
public class EpisodeResponse {
    private Long episodeId;
    private Long projectId;
    private Long epNumber;
    private String epTitle;
    private LocalDateTime createdAt;
}
