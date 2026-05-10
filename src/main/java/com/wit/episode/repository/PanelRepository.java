package com.wit.episode.repository;

import com.wit.episode.domain.Panel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PanelRepository extends JpaRepository<Panel, Long> {
    // 특정 에피소드에 속한 모든 컷을 순서대로 조회
    List<Panel> findByEpisode_EpisodeIdOrderByPanelOrderAsc(Long episodeId);
}
