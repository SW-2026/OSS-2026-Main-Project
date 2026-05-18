package com.wit.background_asset.repository;

import com.wit.background_asset.domain.BackgroundAsset;
import com.wit.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BackgroundAssetRepository extends JpaRepository<BackgroundAsset, Long> {
    List<BackgroundAsset> findAllByMember(Member member);
}