package com.wit.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AiPanelsGenerateRequest(
        @NotBlank String scenarioText,
        @NotNull @Size(max = 20) List<CharacterMention> characters
) {
}
