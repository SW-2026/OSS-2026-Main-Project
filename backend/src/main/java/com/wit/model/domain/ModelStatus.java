package com.wit.model.domain;

public enum ModelStatus {
    PENDING,    // 생성 직후, 학습 대기
    TRAINING,   // 학습 중 (실제 학습 도입 시)
    ACTIVE,     // 학습 완료, 사용 가능
    FAILED      // 학습 실패
}
