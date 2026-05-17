package com.wit.ai.dto;

public record CharacterMention(
        String name,
        Long modelId,
        String triggerWord
) {
}
