package com.wit.episode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HistorySaveRequest {
    private String layoutData;
    private String canvasData;
}
