package com.wit.background_asset.dto;

import jakarta.validation.constraints.NotBlank;

public record BackgroundAssetRequest(
        @NotBlank String assetName,
        @NotBlank String assetUrl
) {}