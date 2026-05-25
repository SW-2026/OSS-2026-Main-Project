package com.wit.background_asset.service;

import com.wit.background_asset.domain.BackgroundAsset;
import com.wit.background_asset.dto.BackgroundAssetRequest;
import com.wit.background_asset.dto.BackgroundAssetResponse;
import com.wit.background_asset.dto.BackgroundAssetUploadRequest;
import com.wit.background_asset.repository.BackgroundAssetRepository;
import com.wit.ai.storage.ImageStorage;
import com.wit.ai.storage.StoredImage;
import com.wit.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BackgroundAssetService {
    private final BackgroundAssetRepository assetRepository;
    private final ImageStorage imageStorage;

    public Long upload(Member member, BackgroundAssetRequest dto) {
        BackgroundAsset asset = BackgroundAsset.builder()
                .member(member)
                .assetName(dto.assetName())
                .assetUrl(dto.assetUrl())
                .build();
        return assetRepository.save(asset).getAssetId();
    }

    @Transactional(readOnly = true)
    public List<BackgroundAssetResponse> getMyAssets(Member member) {
        return assetRepository.findAllByMember(member).stream()
                .map(BackgroundAssetResponse::from)
                .toList();
    }

    public void delete(Member member, Long assetId) {
        BackgroundAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));

        if (!asset.getMember().getMemberId().equals(member.getMemberId())) {
            throw new IllegalStateException("No permission");
        }
        assetRepository.delete(asset);
    }

    public Long uploadMultipart(Member member,
                                BackgroundAssetUploadRequest metadata,
                                MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image required");
        }
        byte[] bytes;
        try {
            bytes = image.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read image", e);
        }
        StoredImage stored = imageStorage.save(bytes, "background", null);

        BackgroundAsset asset = BackgroundAsset.builder()
                .member(member)
                .assetName(metadata.assetName())
                .assetUrl(stored.accessUrl())
                .build();
        return assetRepository.save(asset).getAssetId();
    }
}