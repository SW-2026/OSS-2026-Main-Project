package com.wit.lora.request.dto;

import com.wit.lora.request.domain.LoraRequest;
import com.wit.lora.request.domain.LoraRequestStatus;

import java.time.LocalDateTime;
import java.util.List;

public record LoraRequestResponse(
        Long requestId,
        Long memberId,
        String characterName,
        String triggerWord,
        LoraRequestStatus status,
        int imageCount,
        String imageDir,
        String adminNotes,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        Long loraCatalogId,
        List<String> imageUrls
) {
    public static LoraRequestResponse from(LoraRequest r) {
        return from(r, List.of());
    }

    public static LoraRequestResponse from(LoraRequest r, List<String> imageUrls) {
        return new LoraRequestResponse(
                r.getRequestId(),
                r.getMember().getMemberId(),
                r.getCharacterName(),
                r.getTriggerWord(),
                r.getStatus(),
                r.getImageCount(),
                r.getImageDir(),
                r.getAdminNotes(),
                r.getCreatedAt(),
                r.getCompletedAt(),
                r.getLoraCatalog() != null ? r.getLoraCatalog().getId() : null,
                imageUrls
        );
    }
}
