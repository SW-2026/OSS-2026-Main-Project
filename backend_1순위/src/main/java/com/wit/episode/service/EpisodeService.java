package com.wit.episode.service;

import com.wit.episode.domain.Episode;
import com.wit.episode.domain.Panel;
import com.wit.episode.domain.PanelStatus;
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
    public EpisodeResponse create(Member member, Long projectId, EpisodeCreateRequest request) {
        Project project = validateProjectOwner(member, projectId);

        Episode episode = Episode.builder()
                .project(project)
                .epNumber(request.getEpNumber().intValue())
                .epTitle(request.getEpTitle())
                .build();

        //기본으로 10개의 panel 생성
        for (int i = 1; i <= 10; i++) {
            episode.addPanel(Panel.builder()
                    .panelOrder(i)
                    .status(PanelStatus.PENDING)
                    .build());
        }

        Episode savedEpisode = episodeRepository.save(episode);

        // EpisodeResponse 인자 4개 일치: episodeId, projectId, epNumber, epTitle
        return new EpisodeResponse(
                savedEpisode.getEpisodeId(),
                episode.getProject().getProjectId(),
                (long) savedEpisode.getEpNumber(),
                savedEpisode.getEpTitle()
        );
    }

    public List<EpisodeSummaryResponse> getAll(Member member, Long projectId) {
        validateProjectOwner(member, projectId);

        // SummaryResponse 인자 4개 일치: episodeId, epNumber, epTitle, panelCount[cite: 4]
        return episodeRepository.findByProject_ProjectId(projectId).stream()
                .map(episode -> new EpisodeSummaryResponse(
                        episode.getEpisodeId(),
                        (long) episode.getEpNumber(),
                        episode.getEpTitle(),
                        episode.getPanels().size()
                ))
                .collect(Collectors.toList());
    }

    public EpisodeDetailResponse getOne(Member member, Long episodeId) {
        // 1. 에피소드 존재 확인
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new NoSuchElementException("해당 에피소드를 찾을 수 없습니다. ID: " + episodeId));

        // 2. 소유권 검증
        validateProjectOwner(member, episode.getProject().getProjectId());

        // 3. List<String> 대신 List<PanelResponse> 생성 (사용자님 제공 구조 적용)
        List<EpisodeDetailResponse.PanelResponse> panelResponses = episode.getPanels().stream()
                .map(panel -> new EpisodeDetailResponse.PanelResponse(
                        panel.getPanelId(),        //
                        panel.getPanelOrder(),     //[cite: 3]
                        panel.getStatus(),        //[cite: 3]
                        panel.getFinalImageUrl()   //[cite: 3]
                ))
                .collect(Collectors.toList());

        // 4. DTO 생성자 인자 타입 및 개수 일치 (4개 인자 버전)
        // 에러 메시지 기반 순서: episodeId, epNumber, epTitle, panels
        return new EpisodeDetailResponse(
                episode.getEpisodeId(),           //
                (long) episode.getEpNumber(),     //[cite: 2]
                episode.getEpTitle(),             //[cite: 2]
                panelResponses                    // 마지막 인자로 List<PanelResponse> 전달[cite: 3]
        );
    }

    private Project validateProjectOwner(Member member, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("해당 프로젝트를 찾을 수 없습니다. ID: " + projectId));

        if (!project.getMember().getMemberId().equals(member.getMemberId())) {
            throw new AccessDeniedException("해당 프로젝트에 대한 접근 권한이 없습니다.");
        }
        return project;
    }
}