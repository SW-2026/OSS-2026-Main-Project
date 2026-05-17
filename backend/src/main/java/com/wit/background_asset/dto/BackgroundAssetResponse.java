package com.wit.background_asset.dto;

import com.wit.background_asset.domain.BackgroundAsset;
import java.time.LocalDateTime;

public record BackgroundAssetResponse(
        Long assetId,
        String assetName,
        String assetUrl,
        LocalDateTime createdAt
) {
    public static BackgroundAssetResponse from(BackgroundAsset asset) {
        return new BackgroundAssetResponse(
                asset.getAssetId(),
                asset.getAssetName(),
                asset.getAssetUrl(),
                asset.getCreatedAt()
        );
    }
}