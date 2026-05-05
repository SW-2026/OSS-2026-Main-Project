package com.wit.episode.dto;

import com.wit.episode.domain.PanelStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class EpisodeResponse {
    private Long episodeId;
    private Long projectId;
    private Long epNumber;
    private String epTitle;
}
