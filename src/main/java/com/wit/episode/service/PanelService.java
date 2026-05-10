package com.wit.episode.service;

import com.wit.episode.domain.Episode;
import com.wit.episode.domain.Panel;
import com.wit.episode.domain.PanelStatus;
import com.wit.episode.dto.PanelGenerateRequest;
import com.wit.episode.repository.EpisodeRepository;
import com.wit.episode.repository.PanelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PanelService {

    private final PanelRepository panelRepository;
    private final EpisodeRepository episodeRepository;

    // 시나리오 입력 -> 컷 생성
    @Transactional
    public void generatePanels(Long episodeId, PanelGenerateRequest request) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("에피소드 없음"));
        // 1. 현재 에피소드에 있는 패널 중 가장 높은 순서 값을 가져옴
        int lastOrder = panelRepository.findByEpisode_EpisodeIdOrderByPanelOrderAsc(episodeId)
                .stream()
                .mapToInt(Panel::getPanelOrder)
                .max()
                .orElse(0); // 패널이 하나도 없으면 0부터 시작

        // 2. 임시 패널 생성 (PENDING 상태)
        // 시나리오를 바탕으로 여러 컷이 생성될 수 있으므로 예시로 3컷을 생성한다고 가정
        for (int i = 1; i <= 3; i++) {
            Panel panel = Panel.builder()
                    .panelOrder(lastOrder + i)
                    .status(PanelStatus.PENDING) // 아직 처리 중
                    .prompt(request.getScenario()) // 시나리오를 프롬프트로 임시 저장
                    .build();
            panel.setEpisode(episode);
            panelRepository.save(panel);

            // 2. 실제 AI 호출 로직 (비동기로 실행)
            simulateAiGeneration(panel.getPanelId());
        }
    }

    @Async // 별도 스레드에서 실행됨
    @Transactional
    public void simulateAiGeneration(Long panelId) {
        try {
            // AI 작업을 시뮬레이션 (3초 대기)
            Thread.sleep(3000);

            Panel panel = panelRepository.findById(panelId).orElseThrow();

            // 3. 생성 완료 처리
            // 실제로는 여기서 AI가 준 이미지 URL을 넣어줍니다.
            String mockImageUrl = "https://example.com/ai-generated-" + panelId + ".png";

            // panel 업데이트
            panel.updateResult(mockImageUrl, PanelStatus.COMPLETED);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 1. 컷 목록 조회
    public List<Panel> getPanels(Long episodeId) {
        return panelRepository.findByEpisode_EpisodeIdOrderByPanelOrderAsc(episodeId);
    }

    // 2. 컷 단건 조회(특정 panel 조회)
    public Panel getPanelById(Long panelId) {
        return panelRepository.findById(panelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 패널을 찾을 수 없습니다. ID: " + panelId));
    }

    // 3. 컷 수동 생성 (단일 컷 추가)
    @Transactional
    public Long createPanel(Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 에피소드가 존재하지 않습니다."));

        // 현재 에피소드의 마지막 순서 확인 (간단하게 리스트 사이즈로 계산하거나 DB 조회)
        List<Panel> existingPanels = panelRepository.findByEpisode_EpisodeIdOrderByPanelOrderAsc(episodeId);
        int nextOrder = existingPanels.size() + 1;

        Panel panel = Panel.builder()
                .panelOrder(nextOrder)
                .status(PanelStatus.COMPLETED) // 수동 생성은 바로 편집 가능 상태로 가정
                .build();

        panel.setEpisode(episode); // 연관관계 설정
        return panelRepository.save(panel).getPanelId();
    }

    // 4. 컷 순서 변경
    @Transactional
    public void reorderPanels(Long episodeId, List<Long> panelIds) {
        // 1. 해당 에피소드의 패널들을 가져옴
        List<Panel> panels = panelRepository.findByEpisode_EpisodeIdOrderByPanelOrderAsc(episodeId);

        // 2. 전달받은 ID 순서대로 1부터
        for (int i = 0; i < panelIds.size(); i++) {
            Long panelId = panelIds.get(i);
            int newOrder = i + 1; // 리스트 인덱스 기반으로 1번부터 순서 부여

            Panel panel = panels.stream()
                    .filter(p -> p.getPanelId().equals(panelId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("패널을 찾을 수 없습니다."));

            panel.updateOrder(newOrder);
        }
    }

    // 5. 컷 삭제
    @Transactional
    public void deletePanel(Long panelId) {
        Panel panel = panelRepository.findById(panelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 패널이 없습니다."));

        Long episodeId = panel.getEpisode().getEpisodeId();
        int deletedOrder = panel.getPanelOrder();

        // 1. 패널 삭제
        panelRepository.delete(panel);

        // 2. 삭제된 패널보다 순서가 뒤인 패널들을 한 칸씩 앞으로 당김 (순서 유지)
        List<Panel> remainingPanels = panelRepository.findByEpisode_EpisodeIdOrderByPanelOrderAsc(episodeId);
        for (Panel p : remainingPanels) {
            if (p.getPanelOrder() > deletedOrder) {
                p.updateOrder(p.getPanelOrder() - 1);
            }
        }
    }
}
