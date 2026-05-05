package com.wit.episode.repository;

import com.wit.episode.domain.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    // 기본 CRUD 기능이 자동 포함됩니다.
    List<Episode> findByProject_ProjectId(Long projectId);
}
