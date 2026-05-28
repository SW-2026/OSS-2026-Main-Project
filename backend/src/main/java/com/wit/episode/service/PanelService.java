package com.wit.episode.service;

import com.wit.ai.domain.CharacterAsset;
import com.wit.ai.repository.CharacterAssetRepository;
import com.wit.background_asset.domain.BackgroundAsset;
import com.wit.background_asset.repository.BackgroundAssetRepository;
import com.wit.episode.domain.Episode;
import com.wit.episode.domain.Panel;
import com.wit.episode.domain.PanelStatus;
import com.wit.episode.dto.CutEditorDataResponse;
import com.wit.episode.dto.PanelDetailResponse;
import com.wit.episode.repository.EpisodeRepository;
import com.wit.episode.repository.PanelRepository;
import com.wit.member.domain.Member;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PanelService {

    private final PanelRepository panelRepository;
    private final EpisodeRepository episodeRepository;
    private final CharacterAssetRepository characterAssetRepository;
    private final BackgroundAssetRepository backgroundAssetRepository;

    /**
     * 1단계 placeholder. PanelGenerationService.generate() 사용. 추후 정리 트랙에서 제거 예정.
     */
    @Deprecated
    @Transactional
    public void generatePanels(Long episodeId, String scenario) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 에피소드가 없습니다."));

        // 현재 에피소드의 가장 마지막 패널의 순서를 가져옴(패널이 없으면 0)
        int lastOrder = panelRepository.findMaxOrderByEpisodeId(episodeId).orElse(0);
        // TODO: LLM을 통해 시나리오에서 컷별 파라미터 추출 로직 필요
        // 임시로 PENDING 상태의 패널들을 생성하는 예시입니다.
        Panel newPanel = Panel.builder()
                .panelOrder(lastOrder+1) // 실제로는 추출된 컷 수만큼 루프를 돌며 지정
                .status(PanelStatus.PENDING)
                .scenarioText(scenario)
                .build();

        newPanel.setEpisode(episode);
        panelRepository.save(newPanel);
    }

    /**
     * 2. 컷 목록 조회 (panelOrder 순 정렬)
     */
    public List<Panel> getPanels(Long episodeId) {
        return panelRepository.findByEpisode_EpisodeIdOrderByPanelOrderAsc(episodeId);
    }

    /**
     * 2-1. 컷 목록 조회 + Asset URL enrichment (Phase 1: panel 응답에 캐릭터/배경 URL 포함)
     * N+1 회피 — characterAssetId/backgroundAssetId batch 조회 후 Map lookup
     */
    public List<PanelDetailResponse> getPanelsWithAssets(Long episodeId) {
        List<Panel> panels = panelRepository.findByEpisode_EpisodeIdOrderByPanelOrderAsc(episodeId);

        List<Long> charIds = panels.stream()
                .map(Panel::getCharacterAssetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Long> bgIds = panels.stream()
                .map(Panel::getBackgroundAssetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> charUrlMap = characterAssetRepository.findAllById(charIds).stream()
                .collect(Collectors.toMap(CharacterAsset::getAssetId, CharacterAsset::getImageUrl));
        Map<Long, String> bgUrlMap = backgroundAssetRepository.findAllById(bgIds).stream()
                .collect(Collectors.toMap(BackgroundAsset::getAssetId, BackgroundAsset::getAssetUrl));

        return panels.stream()
                .map(p -> PanelDetailResponse.from(
                        p,
                        p.getCharacterAssetId() != null ? charUrlMap.get(p.getCharacterAssetId()) : null,
                        p.getBackgroundAssetId() != null ? bgUrlMap.get(p.getBackgroundAssetId()) : null))
                .toList();
    }

    /**
     * 3. 컷 단건 조회
     */
    public Panel getPanel(Long panelId) {
        return panelRepository.findById(panelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 패널이 없습니다. id=" + panelId));
    }

    /**
     * 3-1. 컷 단건 조회 + Asset URL enrichment (Phase 1)
     * 단건은 N+1 무관 — Asset 별로 findById 2번
     */
    public PanelDetailResponse getPanelWithAssets(Long panelId) {
        Panel panel = getPanel(panelId);
        String charUrl = panel.getCharacterAssetId() != null
                ? characterAssetRepository.findById(panel.getCharacterAssetId())
                        .map(CharacterAsset::getImageUrl).orElse(null)
                : null;
        String bgUrl = panel.getBackgroundAssetId() != null
                ? backgroundAssetRepository.findById(panel.getBackgroundAssetId())
                        .map(BackgroundAsset::getAssetUrl).orElse(null)
                : null;
        return PanelDetailResponse.from(panel, charUrl, bgUrl);
    }

    /**
     * 4. 컷 수동 생성
     */
    @Transactional
    public Long createPanel(Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 에피소드가 없습니다."));

        // 마지막 순서 다음으로 지정
        int lastOrder = panelRepository.findMaxOrderByEpisodeId(episodeId).orElse(0);

        Panel panel = Panel.builder()
                .panelOrder(lastOrder + 1)
                .status(PanelStatus.CREATED)
                .build();

        panel.setEpisode(episode);
        return panelRepository.save(panel).getPanelId();
    }

    /**
     * 5. 컷 순서 변경 (panelOrder 배열로 전달받아 일괄 갱신)
     */
    @Transactional
    public void reorderPanels(Long episodeId, List<Long> panelIds) {
        List<Panel> panels = panelRepository.findByEpisode_EpisodeId(episodeId);

        // ID를 키로 하는 맵 생성
        Map<Long, Panel> panelMap = panels.stream()
                .collect(Collectors.toMap(Panel::getPanelId, p -> p));

        // 전달받은 리스트 순서대로 panelOrder 부여
        for (int i = 0; i < panelIds.size(); i++) {
            Panel panel = panelMap.get(panelIds.get(i));
            if (panel != null) {
                // 엔티티에 updateOrder 메서드가 필요합니다 (아래 추가 가이드 참고)
                panel.updateOrder(i + 1);
            }
        }
    }

    /**
     * 6. 컷 삭제
     */
    @Transactional
    public void deletePanel(Long panelId) {
        Panel panel = panelRepository.findById(panelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 패널이 없습니다."));
        panelRepository.delete(panel);
    }

    /**
     * 7. 컷 레이어 데이터(layoutData) 업데이트
     * 사용자가 캔버스를 편집하고 저장할 때 호출됩니다.
     */
    @Transactional
    public void updatePanelLayout(Long panelId, String layoutData) {
        Panel panel = getPanel(panelId);
        panel.updateLayoutData(layoutData);
    }

    /**
     * 8. 컷 편집기 데이터(strokes/balloons/canvasImages/layers JSON) 저장
     * frontend supabase 대체. 권한 체크는 validatePanelAccess에서.
     */
    @Transactional
    public void saveCutEditorData(Member member, Long panelId, String cutEditorData) {
        Panel panel = validatePanelAccess(member, panelId);
        panel.updateCutEditorData(cutEditorData);
    }

    /**
     * 9. 컷 편집기 데이터 조회 (없으면 cutEditorData=null로 반환)
     */
    public CutEditorDataResponse getCutEditorData(Member member, Long panelId) {
        Panel panel = validatePanelAccess(member, panelId);
        return CutEditorDataResponse.from(panel);
    }

    /**
     * 패널 소유 검증 — Panel → Episode → Project → Member 체인 확인
     * 미존재: EntityNotFoundException(404). 타인 소유: AccessDeniedException(403)
     * getPanel은 IllegalArgumentException(400)을 던지는 known_issue가 있어 별도 메서드로 분리
     */
    private Panel validatePanelAccess(Member member, Long panelId) {
        Panel panel = panelRepository.findById(panelId)
                .orElseThrow(() -> new EntityNotFoundException("해당 패널을 찾을 수 없습니다. ID: " + panelId));
        if (!panel.getEpisode().getProject().getMember().getMemberId().equals(member.getMemberId())) {
            throw new AccessDeniedException("해당 패널에 대한 접근 권한이 없습니다.");
        }
        return panel;
    }
}
