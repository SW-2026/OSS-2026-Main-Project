package com.wit.episode.repository;

import com.wit.episode.domain.Panel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PanelRepository extends JpaRepository<Panel, Long> {

    // 1. 특정 에피소드에 속한 모든 컷을 순서대로 조회 (명세서의 '컷 목록 조회')
    List<Panel> findByEpisode_EpisodeIdOrderByPanelOrderAsc(Long episodeId);

    // 2. 특정 에피소드에 속한 모든 컷 조회 (순서 상관 없이 맵핑용으로 사용)
    List<Panel> findByEpisode_EpisodeId(Long episodeId);

    /**
     * 3. 특정 에피소드 내에서 현재 가장 큰 panelOrder 값을 조회
     * '컷 수동 생성' 시 마지막 순서 뒤에 붙이기 위해 사용합니다.
     */
    @Query("SELECT MAX(p.panelOrder) FROM Panel p WHERE p.episode.episodeId = :episodeId")
    Optional<Integer> findMaxOrderByEpisodeId(@Param("episodeId") Long episodeId);
}