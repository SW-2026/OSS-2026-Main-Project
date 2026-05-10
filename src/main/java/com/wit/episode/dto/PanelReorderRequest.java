package com.wit.episode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PanelReorderRequest {
    private List<Long> panelIds; // 변경된 순서대로 담긴 ID 리스트
}
