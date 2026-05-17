package com.wit.ai.dto;

public record ComposedPrompt(
        String positivePrompt,
        String negativePrompt,
        long seed,
        String loraName
) {
}
