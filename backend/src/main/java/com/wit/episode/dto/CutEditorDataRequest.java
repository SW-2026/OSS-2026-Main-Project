package com.wit.episode.dto;

import jakarta.validation.constraints.NotNull;

public record CutEditorDataRequest(
        @NotNull String cutEditorData   // frontend가 직렬화한 {strokes,balloons,canvasImages,layers} JSON 문자열
) {
}
