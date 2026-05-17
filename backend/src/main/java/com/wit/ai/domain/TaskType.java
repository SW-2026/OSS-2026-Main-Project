package com.wit.ai.domain;

public enum TaskType {
    CHARACTER,      // POST /api/ai/character — 캐릭터 단독 생성
    PANELS,         // POST /api/episodes/{id}/panels/generate — 10컷 마스터
    COMPOSE,        // POST /api/ai/compose — 옵션
    BACKGROUND,     // POST /api/ai/background — 임시
    SEGMENT,        // POST /api/ai/segment — mock
    REFINE          // POST /api/ai/refine — mock
}
