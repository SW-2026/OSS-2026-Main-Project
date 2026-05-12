package com.wit.episode.service;

import com.wit.episode.domain.Panel;
import com.wit.episode.domain.PanelHistory;
import com.wit.episode.dto.HistoryResponse;
import com.wit.episode.dto.HistoryRestore;
import com.wit.episode.repository.HistoryRepository;
import com.wit.episode.repository.PanelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final PanelRepository panelRepository; // Panel 존재 여부 확인용

    /**
     * 1. 히스토리 저장
     * 새 히스토리를 저장할 때 자동으로 버전을 계산하여 저장합니다.
     */
    @Transactional
    public void saveHistory(Long panelId, String layoutData, String canvasData) {
        Optional<Integer> maxVersion = historyRepository.findMaxVersionByPanelId(panelId);
        if (maxVersion.isPresent()) {
            PanelHistory latest = historyRepository.findByPanel_PanelIdAndVersion(panelId, maxVersion.get()).get();
            if (latest.getLayoutData().equals(layoutData)) {
                return; // 최신 데이터와 동일하면 저장하지 않음
            }
        }

        Panel panel = panelRepository.findById(panelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 패널이 존재하지 않습니다."));

        // 현재 최대 버전을 조회 후 +1 (없으면 1부터 시작)
        Integer nextVersion = historyRepository.findMaxVersionByPanelId(panelId)
                .map(v -> v + 1)
                .orElse(1);

        PanelHistory history = PanelHistory.builder()
                .panel(panel)
                .version(nextVersion)
                .layoutData(layoutData)
                .canvasData(canvasData)
                .build();

        historyRepository.save(history);
    }

    /**
     * 2. 히스토리 목록 조회
     */
    public List<HistoryResponse> getHistoryList(Long panelId) {
        return historyRepository.findAllByPanel_PanelIdOrderByVersionDesc(panelId).stream()
                .map(HistoryResponse::from) // Entity -> DTO 변환
                .collect(Collectors.toList());
    }

    /**
     * 3. 특정 버전으로 복원, 새로운 버전으로 저장(기존 버전 유지)
     */
    @Transactional
    public HistoryRestore restoreHistory(Long panelId, int version) {
        // 1. 복원하고자 하는 과거의 데이터를 조회
        PanelHistory targetHistory = historyRepository.findByPanel_PanelIdAndVersion(panelId, version)
                .orElseThrow(() -> new IllegalArgumentException("해당 버전의 히스토리가 존재하지 않습니다."));

        // 2. 해당 데이터를 '새로운 버전'으로 재저장
        // 기존에 만들어둔 saveHistory 로직을 활용하거나 직접 구현합니다.
        String layoutDataToRestore = targetHistory.getLayoutData();
        String canvasDataToRestore = targetHistory.getCanvasData();
        saveHistory(panelId, layoutDataToRestore, canvasDataToRestore);

        // 3. 프론트엔드에서 캔버스에 즉시 적용할 수 있도록 데이터를 반환
        return HistoryRestore.of(layoutDataToRestore, canvasDataToRestore);
    }
}