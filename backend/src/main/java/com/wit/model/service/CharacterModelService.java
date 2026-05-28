package com.wit.model.service;

import com.wit.lora.domain.LoraCatalog;
import com.wit.lora.repository.LoraCatalogRepository;
import com.wit.member.domain.Member;
import com.wit.model.domain.CharacterModel;
import com.wit.model.domain.ModelStatus;
import com.wit.model.dto.CharacterModelCreateRequest;
import com.wit.model.dto.CharacterModelDetailResponse;
import com.wit.model.dto.CharacterModelStatusResponse;
import com.wit.model.dto.CharacterModelSummaryResponse;
import com.wit.model.repository.CharacterModelRepository;
import com.wit.project.domain.Project;
import com.wit.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 기본은 읽기 전용 — 변경 메서드만 @Transactional로 덮어씀
public class CharacterModelService {

    private final CharacterModelRepository characterModelRepository;
    private final ProjectRepository projectRepository;
    private final LoraCatalogRepository loraCatalogRepository;

    /**
     * 모델 생성 — 메타데이터 저장 + 레퍼런스 이미지는 1.5단계에서 mock(저장 안 함).
     * 실제 LoRA 학습은 N단계에서 추가.
     */
    @Transactional
    public CharacterModelDetailResponse create(Member member, Long projectId,
                                               CharacterModelCreateRequest request,
                                               MultipartFile[] images) {
        Project project = validateProjectAccess(member, projectId);
        validateImages(images);

        CharacterModel saved = characterModelRepository.save(
                CharacterModel.builder()
                        .project(project)
                        .modelName(request.getModelName())
                        .triggerWord(request.getTriggerWord())
                        .loraModelPath(request.getLoraModelPath())
                        .appearancePrompt(request.getAppearancePrompt())
                        .outfitPrompt(request.getOutfitPrompt())
                        .status(ModelStatus.PENDING)
                        .build()
        );
        return toDetailResponse(saved);
    }

    /**
     * Project의 모델 목록 조회 — 경량 응답(modelId, modelName, status, createdAt).
     */
    public List<CharacterModelSummaryResponse> findAllByProject(Member member, Long projectId) {
        Project project = validateProjectAccess(member, projectId);
        return characterModelRepository.findAllByProject(project).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    /**
     * 모델 단건 상세 조회.
     */
    public CharacterModelDetailResponse findById(Member member, Long modelId) {
        CharacterModel model = validateModelAccess(member, modelId);
        return toDetailResponse(model);
    }

    /**
     * LoRA 카탈로그 기반 자동 등록 — 소재 탭에서 LoRA 카드 클릭 시 호출.
     * 이미지/모델명 입력 없이 LoraCatalog 정보(displayName/triggerWord/fileName)로 자동 채움.
     * 같은 프로젝트에 같은 LoRA 이미 등록됐으면 기존 모델 반환 (멱등).
     */
    @Transactional
    public CharacterModelDetailResponse createFromLora(Member member, Long projectId, String loraFileName) {
        Project project = validateProjectAccess(member, projectId);

        LoraCatalog lora = loraCatalogRepository.findByFileName(loraFileName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 LoRA를 찾을 수 없습니다. fileName: " + loraFileName));

        return characterModelRepository.findByProjectAndLoraModelPath(project, lora.getFileName())
                .map(this::toDetailResponse)
                .orElseGet(() -> {
                    CharacterModel saved = characterModelRepository.save(
                            CharacterModel.builder()
                                    .project(project)
                                    .modelName(lora.getDisplayName())
                                    .triggerWord(lora.getTriggerWord())
                                    .loraModelPath(lora.getFileName())
                                    .status(ModelStatus.ACTIVE)  // 학습 mock — 즉시 ACTIVE
                                    .build()
                    );
                    return toDetailResponse(saved);
                });
    }

    /**
     * 학습 트리거 — 현재 mock(즉시 ACTIVE로 변경, loraModelPath 미설정).
     * 실제 LoRA 학습은 N단계.
     */
    @Transactional
    public CharacterModelStatusResponse train(Member member, Long modelId) {
        CharacterModel model = validateModelAccess(member, modelId);
        model.markActive(); // dirty checking으로 자동 저장
        return new CharacterModelStatusResponse(model.getModelId(), model.getStatus());
    }

    // ===== 권한 검증 (ProjectService.validateProjectOwner 대신 inline 구현) =====

    private Project validateProjectAccess(Member member, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 프로젝트를 찾을 수 없습니다. ID: " + projectId));
        if (!project.getMember().getMemberId().equals(member.getMemberId())) {
            throw new AccessDeniedException("해당 프로젝트에 대한 접근 권한이 없습니다.");
        }
        return project;
    }

    private CharacterModel validateModelAccess(Member member, Long modelId) {
        CharacterModel model = characterModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 모델을 찾을 수 없습니다. ID: " + modelId));
        if (!model.getProject().getMember().getMemberId().equals(member.getMemberId())) {
            throw new AccessDeniedException("해당 모델에 대한 접근 권한이 없습니다.");
        }
        return model;
    }

    /**
     * 이미지 검증 — 1.5단계는 개수만 확인. 포맷/크기 검증은 실제 학습 구현 시 추가.
     */
    private void validateImages(MultipartFile[] images) {
        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("최소 1개의 레퍼런스 이미지가 필요합니다.");
        }
    }

    // ===== 매핑 =====

    private CharacterModelDetailResponse toDetailResponse(CharacterModel m) {
        return new CharacterModelDetailResponse(
                m.getModelId(),
                m.getProject().getProjectId(),
                m.getModelName(),
                m.getTriggerWord(),
                m.getLoraModelPath(),
                m.getAppearancePrompt(),
                m.getOutfitPrompt(),
                m.getStatus(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }

    private CharacterModelSummaryResponse toSummaryResponse(CharacterModel m) {
        return new CharacterModelSummaryResponse(
                m.getModelId(),
                m.getModelName(),
                m.getStatus(),
                m.getCreatedAt()
        );
    }
}
