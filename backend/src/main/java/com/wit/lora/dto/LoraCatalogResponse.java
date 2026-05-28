package com.wit.lora.dto;

import com.wit.lora.domain.LoraCatalog;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoraCatalogResponse {

    private final Long id;
    private final String fileName;
    private final String displayName;
    private final String thumbnailUrl;
    private final String triggerWord;
    private final String description;

    @Builder
    private LoraCatalogResponse(Long id, String fileName, String displayName,
                                String thumbnailUrl, String triggerWord, String description) {
        this.id = id;
        this.fileName = fileName;
        this.displayName = displayName;
        this.thumbnailUrl = thumbnailUrl;
        this.triggerWord = triggerWord;
        this.description = description;
    }

    public static LoraCatalogResponse from(LoraCatalog lora) {
        return LoraCatalogResponse.builder()
                .id(lora.getId())
                .fileName(lora.getFileName())
                .displayName(lora.getDisplayName())
                .thumbnailUrl(lora.getThumbnailUrl())
                .triggerWord(lora.getTriggerWord())
                .description(lora.getDescription())
                .build();
    }
}
