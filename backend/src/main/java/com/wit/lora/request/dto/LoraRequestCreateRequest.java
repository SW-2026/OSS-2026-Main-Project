package com.wit.lora.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 신청 메타데이터 (multipart "metadata" part)
public record LoraRequestCreateRequest(
        @NotBlank @Size(max = 100) String characterName,
        @NotBlank @Size(max = 1000) String triggerWord
) {}
