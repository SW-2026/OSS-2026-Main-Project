package com.wit.background_asset.service;

import com.wit.background_asset.domain.BackgroundAsset;
import com.wit.background_asset.dto.BackgroundAssetRequest;
import com.wit.background_asset.dto.BackgroundAssetResponse;
import com.wit.background_asset.repository.BackgroundAssetRepository;
import com.wit.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BackgroundAssetService {
    private final BackgroundAssetRepository assetRepository;

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
}