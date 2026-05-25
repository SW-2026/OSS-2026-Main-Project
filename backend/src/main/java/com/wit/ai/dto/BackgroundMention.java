package com.wit.ai.dto;

public record BackgroundMention(
        String name,
        Long assetId,
        String assetUrl
) {
    public BackgroundMention(String name, Long assetId) {
        this(name, assetId, null);
    }
}
