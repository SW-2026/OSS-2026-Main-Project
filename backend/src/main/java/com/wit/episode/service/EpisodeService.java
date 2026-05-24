package com.wit.episode.service;

import com.wit.episode.domain.Episode;
import com.wit.episode.dto.*;
import com.wit.episode.repository.EpisodeRepository;
import com.wit.member.domain.Member;
import com.wit.project.domain.Project;
import com.wit.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EpisodeService {

    private final EpisodeRepository episodeRepository;
    private final ProjectRepository projectRepository;


    @Transactional

    //episode create(POST)
    public EpisodeResponse create(Member member, Long projectId, EpisodeCreateRequest request) {
        Project project = validateProjectOwner(member, projectId);

        Episode episode = Episode.builder()
                .project(project)
                .epNumber(request.getEpNumber().intValue())
                .epTitle(request.getEpTitle())
                .build();

        Episode savedEpisode = episodeRepository.save(episode);

        //EpisodeResponse
        return new EpisodeResponse(
                savedEpisode.getEpisodeId(),
                episode.getProject().getProjectId(),
                (long) savedEpisode.getEpNumber(),
                savedEpisode.getEpTitle(),
                savedEpisode.getCreatedAt()
        );
    }

    //전제 Episode 조회(EpisodeSummaryResponse)
    public List<EpisodeSummaryResponse> getAll(Member member, Long projectId) {
        validateProjectOwner(member, projectId);

        return episodeRepository.findByProject_ProjectId(projectId).stream()
                .map(episode -> new EpisodeSummaryResponse(
                        episode.getEpisodeId(),
                        (long) episode.getEpNumber(),
                        episode.getEpTitle(),
                        episode.getPanels().size(),
                        episode.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    //하나의 Episode 정보 조회(EpisodeDetailResponse
    public EpisodeDetailResponse getOne(Member member, Long episodeId) {
        // 1. 에피소드 존재 확인
        Episode episode = episodeRepository.findByIdWithPanels(episodeId)
                .orElseThrow(() -> new NoSuchElementException("해당 에피소드를 찾을 수 없습니다. ID: " + episodeId));

        // 2. 소유권 검증
        validateProjectOwner(member, episode.getProject().getProjectId());

        List<EpisodeDetailResponse.PanelResponse> panelResponses = episode.getPanels().stream()
                .map(panel -> new EpisodeDetailResponse.PanelResponse(
                        panel.getPanelId(),
                        panel.getPanelOrder(),
                        panel.getStatus(),
                        panel.getFinalImageUrl()
                ))
                .collect(Collectors.toList());

        return new EpisodeDetailResponse(
                episode.getEpisodeId(),
                (long) episode.getEpNumber(),
                episode.getEpTitle(),
                panelResponses
        );
    }

    // Episode 부분 수정 (PATCH) — null이 아닌 필드만 반영, 변경감지(dirty checking)로 자동 저장
    @Transactional
    public EpisodeResponse update(Member member, Long episodeId, EpisodeUpdateRequest request) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 에피소드를 찾을 수 없습니다. ID: " + episodeId));

        validateProjectOwner(member, episode.getProject().getProjectId());

        episode.updatePartial(
                request.getEpNumber() != null ? request.getEpNumber().intValue() : null,
                request.getEpTitle()
        );

        return new EpisodeResponse(
                episode.getEpisodeId(),
                episode.getProject().getProjectId(),
                (long) episode.getEpNumber(),
                episode.getEpTitle(),
                episode.getCreatedAt()
        );
    }

    // Episode 삭제 (DELETE) — 본인 프로젝트의 에피소드만 삭제 가능,
    // Episode.panels의 orphanRemoval=true + CascadeType.ALL로 하위 panel도 함께 제거
    @Transactional
    public void delete(Member member, Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 에피소드를 찾을 수 없습니다. ID: " + episodeId));

        validateProjectOwner(member, episode.getProject().getProjectId());

        episodeRepository.delete(episode);
    }

    private Project validateProjectOwner(Member member, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다. ID: " + projectId));

        if (!project.getMember().getMemberId().equals(member.getMemberId())) {
            throw new AccessDeniedException("해당 프로젝트에 대한 접근 권한이 없습니다.");
        }
        return project;
    }
}