package com.wit.lora.request.dto;

import com.wit.lora.request.domain.LoraRequestStatus;
import jakarta.validation.constraints.NotNull;

// [관리자] 신청 상태/메모 변경 요청 (Phase 1.5)
public record LoraRequestUpdateRequest(
        @NotNull LoraRequestStatus status,
        String adminNotes,
        Long loraCatalogId   // COMPLETED 시 연결할 카탈로그 (선택)
) {}
