package com.wit.episode.repository;

import com.wit.episode.domain.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    // 기본 CRUD 기능이 자동 포함됩니다.
    List<Episode> findByProject_ProjectId(Long projectId);

    //에피소드 상세 조회 시 연관된 패널들을 한 번의 쿼리로 가져오기 (성능 최적화)
    @Query("select e from Episode e left join fetch e.panels where e.episodeId = :episodeId")
    Optional<Episode> findByIdWithPanels(@Param("episodeId") Long episodeId);
}
