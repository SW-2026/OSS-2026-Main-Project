package com.wit.project.service;

import com.wit.member.domain.Member;
import com.wit.project.domain.Project;
import com.wit.project.dto.ProjectCreateRequest;
import com.wit.project.dto.ProjectDetailResponse;
import com.wit.project.dto.ProjectResponse;
import com.wit.project.dto.ProjectSummaryResponse;
import com.wit.project.dto.ProjectUpdateRequest;
import com.wit.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 레벨 기본은 읽기 전용 — 변경 메서드에서만 @Transactional로 덮어씀
public class ProjectService {

    private final ProjectRepository projectRepository;

    // 프로젝트 생성 (POST)
    @Transactional
    public ProjectResponse create(Member member, ProjectCreateRequest request) {
        // 1. 빌더로 새 프로젝트 엔티티 생성 — 소유자는 현재 로그인한 회원
        Project project = Project.builder()
                .member(member)
                .title(request.getTitle())
                .genre(request.getGenre())
                .build();

        // 2. 저장 후 생성된 PK·createdAt이 채워진 엔티티를 응답 DTO로 변환
        Project saved = projectRepository.save(project);

        return new ProjectResponse(
                saved.getProjectId(),
                saved.getMember().getMemberId(),
                saved.getTitle(),
                saved.getGenre(),
                saved.getCreatedAt()
        );
    }

    // 내 프로젝트 목록 조회 (GET) — 본인 소유 프로젝트만 반환
    public List<ProjectSummaryResponse> getMyProjects(Member member) {
        return projectRepository
                .findByMember_MemberIdOrderByCreatedAtDesc(member.getMemberId())
                .stream()
                .map(p -> new ProjectSummaryResponse(
                        p.getProjectId(),
                        p.getTitle(),
                        p.getGenre(),
                        p.getEpisodes().size(), // 연관된 회차 수
                        p.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // 프로젝트 상세 조회 (GET) — 회차 요약 리스트 포함
    public ProjectDetailResponse getOne(Member member, Long projectId) {
        // 소유권 검증 후 프로젝트 반환
        Project project = validateProjectOwner(member, projectId);

        // 연관된 회차들을 간략 DTO로 매핑
        List<ProjectDetailResponse.EpisodeBrief> episodeBriefs = project.getEpisodes().stream()
                .map(e -> new ProjectDetailResponse.EpisodeBrief(
                        e.getEpisodeId(),
                        e.getEpNumber(),
                        e.getEpTitle()
                ))
                .collect(Collectors.toList());

        return new ProjectDetailResponse(
                project.getProjectId(),
                project.getTitle(),
                project.getGenre(),
                project.getStyleBasePrompt(),
                project.getNegativePrompt(),
                project.getBackgroundPrompt(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                episodeBriefs
        );
    }

    // 프로젝트 삭제 (DELETE) — 본인 프로젝트만 삭제 가능, cascade 설정으로 하위 회차/패널도 함께 제거
    @Transactional
    public void delete(Member member, Long projectId) {
        Project project = validateProjectOwner(member, projectId);
        projectRepository.delete(project);
    }

    // 프로젝트 부분 수정 (PATCH) — null이 아닌 필드만 반영, 변경감지(dirty checking)로 자동 저장
    @Transactional
    public ProjectDetailResponse update(Member member, Long projectId, ProjectUpdateRequest request) {
        // 1) 소유권 검증 — 미존재 시 IllegalArgumentException(400), 소유자 아닐 시 AccessDeniedException(403)
        Project project = validateProjectOwner(member, projectId);

        // 2) 엔티티의 updatePartial로 null이 아닌 필드만 덮어씀
        project.updatePartial(
                request.getTitle(),
                request.getGenre(),
                request.getStyleBasePrompt(),
                request.getNegativePrompt(),
                request.getBackgroundPrompt()
        );

        // 3) 회차 요약 매핑 후 상세 응답 DTO로 변환 (1단계 종료 후 getOne과의 중복은 별도 리팩터링 예정)
        List<ProjectDetailResponse.EpisodeBrief> episodeBriefs = project.getEpisodes().stream()
                .map(e -> new ProjectDetailResponse.EpisodeBrief(
                        e.getEpisodeId(),
                        e.getEpNumber(),
                        e.getEpTitle()
                ))
                .collect(Collectors.toList());

        return new ProjectDetailResponse(
                project.getProjectId(),
                project.getTitle(),
                project.getGenre(),
                project.getStyleBasePrompt(),
                project.getNegativePrompt(),
                project.getBackgroundPrompt(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                episodeBriefs
        );
    }

    /**
     * EpisodeService의 동일 이름 메서드와 같은 패턴.
     * 1) 프로젝트가 존재하지 않으면 EntityNotFoundException → GlobalExceptionHandler가 404로 매핑
     * 2) 소유자가 아니면 AccessDeniedException → GlobalExceptionHandler가 403으로 매핑
     */
    private Project validateProjectOwner(Member member, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("해당 프로젝트를 찾을 수 없습니다. ID: " + projectId));

        if (!project.getMember().getMemberId().equals(member.getMemberId())) {
            throw new AccessDeniedException("해당 프로젝트에 대한 접근 권한이 없습니다.");
        }
        return project;
    }
}
