package com.wit.ai.dto;

public record CharacterMention(
        String name,
        Long modelId,
        String triggerWord,
        String loraModelPath
) {
    public CharacterMention(String name, Long modelId, String triggerWord) {
        this(name, modelId, triggerWord, null);
    }
}
