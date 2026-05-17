package com.wit.episode.service;

import com.wit.episode.domain.Episode;
import com.wit.episode.domain.Panel;
import com.wit.episode.domain.PanelStatus;
import com.wit.episode.repository.EpisodeRepository;
import com.wit.episode.repository.PanelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PanelService {

    private final PanelRepository panelRepository;
    private final EpisodeRepository episodeRepository;

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
     * 3. 컷 단건 조회
     */
    public Panel getPanel(Long panelId) {
        return panelRepository.findById(panelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 패널이 없습니다. id=" + panelId));
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
}
