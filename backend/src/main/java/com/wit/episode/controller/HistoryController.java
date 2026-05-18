package com.wit.episode.controller;

import com.wit.episode.dto.HistoryResponse;
import com.wit.episode.dto.HistoryRestore;
import com.wit.episode.dto.HistorySaveRequest; // 저장을 위한 새로운 DTO (추천)
import com.wit.episode.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/panels/{panelId}/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    /**
     * 1. 히스토리 저장 (POST)
     * 프론트엔드에서 현재 캔버스의 layoutData와 canvasData를 받아 저장합니다.
     */
    @PostMapping
    public ResponseEntity<Void> saveHistory(
            @PathVariable Long panelId,
            @RequestBody HistorySaveRequest request) {

        historyService.saveHistory(panelId, request.getLayoutData(), request.getCanvasData());
        return ResponseEntity.ok().build();
    }

    /**
     * 2. 히스토리 목록 조회 (GET)
     * 해당 패널의 모든 버전 리스트를 반환합니다.
     */
    @GetMapping
    public ResponseEntity<List<HistoryResponse>> getHistoryList(@PathVariable Long panelId) {
        List<HistoryResponse> historyList = historyService.getHistoryList(panelId);
        return ResponseEntity.ok(historyList);
    }

    /**
     * 3. 특정 버전 복원 (POST)
     * 선택한 버전의 데이터를 가져오고, 동시에 새로운 버전으로 저장합니다.
     */
    @PostMapping("/{version}/restore")
    public ResponseEntity<HistoryRestore> restoreHistory(
            @PathVariable Long panelId,
            @PathVariable int version) {

        HistoryRestore restoredData = historyService.restoreHistory(panelId, version);
        return ResponseEntity.ok(restoredData);
    }
}
