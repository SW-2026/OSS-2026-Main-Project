package com.wit.background_asset.dto;

import jakarta.validation.constraints.NotBlank;

public record BackgroundAssetUploadRequest(
        @NotBlank String assetName
) {}
