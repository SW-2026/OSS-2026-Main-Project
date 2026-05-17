package com.wit.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ComfyUIOrchestrator {

    /**
     * 캐릭터 단독 생성 비동기 처리.
     * 2-10c에서 본체 구현 — workflow 조립, ComfyUI 호출, 폴링, 저장, AiTask 갱신.
     */
    @Async
    public void processCharacterGeneration(Long taskId, Long modelId,
                                           String poseTags, String backgroundTags) {
        log.info("ComfyUIOrchestrator.processCharacterGeneration stub — taskId={}, modelId={}",
                taskId, modelId);
    }
}
