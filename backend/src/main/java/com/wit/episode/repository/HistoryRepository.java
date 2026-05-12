package com.wit.episode.repository;

import com.wit.episode.domain.PanelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends JpaRepository<PanelHistory, Long> {

    // 특정 패널의 모든 히스토리 목록 조회 (최신순)
    List<PanelHistory> findAllByPanel_PanelIdOrderByVersionDesc(Long panelId);

    // 가장 최근 버전 번호 찾기 (새 버전 저장 시 사용)
    @Query("SELECT MAX(h.version) FROM PanelHistory h WHERE h.panel.panelId = :panelId")
    Optional<Integer> findMaxVersionByPanelId(@Param("panelId") Long panelId);

    // 특정 버전의 히스토리 단건 조회 (복원 시 사용)
    Optional<PanelHistory> findByPanel_PanelIdAndVersion(Long panelId, int version);
}
