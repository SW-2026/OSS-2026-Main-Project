package com.wit.episode.service;

import com.wit.episode.domain.Episode;
import com.wit.episode.repository.EpisodeRepository;
import com.wit.project.domain.Project;
import com.wit.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 조회 성능 최적화
public class EpisodeService {

    private final EpisodeRepository episodeRepository;
    private final ProjectRepository projectRepository;

    /**
     * 1. Episode 생성 (Create)
     */
    @Transactional
    public Episode create(Long projectId, Integer epNumber, String epTitle, String content) {
        // 프로젝트 존재 여부 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("해당 프로젝트를 찾을 수 없습니다. ID: " + projectId));

        // 엔티티 빌드 및 저장
        Episode episode = Episode.builder()
                .project(project)
                .epNumber(epNumber)
                .epTitle(epTitle)
                .content(content)
                .build();

        return episodeRepository.save(episode);
    }

    /**
     * 2. 전체 Episode 조회 (GetAll)
     */
    public List<Episode> getAll() {
        return episodeRepository.findAll();
    }

    /**
     * 3. 특정 Episode 상세 조회 (GetOne)
     */
    public Episode getOne(Long episodeId) {
        return episodeRepository.findById(episodeId)
                .orElseThrow(() -> new NoSuchElementException("해당 에피소드를 찾을 수 없습니다. ID: " + episodeId));
    }
}
