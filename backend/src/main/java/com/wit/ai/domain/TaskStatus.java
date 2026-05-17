package com.wit.ai.domain;

public enum TaskStatus {
    PENDING,        // 작업 생성 직후, 대기 중
    PROCESSING,     // ComfyUI/LLM 처리 중
    COMPLETED,      // 완료, resultUrl + targetId 채워짐
    FAILED          // 실패, errorMessage 채워짐
}
