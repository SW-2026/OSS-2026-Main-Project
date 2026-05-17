package com.wit.ai.repository;

import com.wit.ai.domain.CharacterAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterAssetRepository extends JpaRepository<CharacterAsset, Long> {
}
