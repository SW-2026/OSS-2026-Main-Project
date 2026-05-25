package com.wit.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AiPanelsGenerateRequest(
        @NotBlank String scenarioText,
        @NotNull @Size(max = 20) List<CharacterMention> characters,
        @Size(max = 20) List<BackgroundMention> backgrounds
) {
    // 기존 2-arg 호출 호환 — backgrounds 기본 empty list (CharacterMention 패턴)
    public AiPanelsGenerateRequest(String scenarioText, List<CharacterMention> characters) {
        this(scenarioText, characters, List.of());
    }
}
