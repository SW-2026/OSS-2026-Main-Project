package com.wit.lora.request.domain;

public enum LoraRequestStatus {
    PENDING,    // 신청 접수 (Phase 1 기본)
    TRAINING,   // 학습 중 (Phase 2)
    COMPLETED,  // 완료 — lora_catalog 연결
    FAILED,     // 실패
    REJECTED    // 관리자 반려
}
