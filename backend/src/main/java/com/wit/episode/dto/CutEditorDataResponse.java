package com.wit.episode.dto;

import com.wit.episode.domain.Panel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CutEditorDataResponse {

    private final String cutEditorData;

    @Builder
    private CutEditorDataResponse(String cutEditorData) {
        this.cutEditorData = cutEditorData;
    }

    public static CutEditorDataResponse from(Panel panel) {
        return CutEditorDataResponse.builder()
                .cutEditorData(panel.getCutEditorData())
                .build();
    }
}
