package com.wit.lora.request.controller;

import com.wit.auth.dto.PrincipalDetails;
import com.wit.global.response.ApiResponse;
import com.wit.lora.request.domain.LoraRequestStatus;
import com.wit.lora.request.dto.LoraRequestCreateRequest;
import com.wit.lora.request.dto.LoraRequestResponse;
import com.wit.lora.request.dto.LoraRequestUpdateRequest;
import com.wit.lora.request.service.LoraRequestService;
import com.wit.member.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoraRequestController {

    private final LoraRequestService loraRequestService;

    // LoRA 신청 생성 (multipart: metadata + images[]) — 201 CREATED
    @PostMapping(value = "/lora-requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LoraRequestResponse>> create(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestPart("metadata") LoraRequestCreateRequest metadata,
            @RequestPart("images") MultipartFile[] images) {
        Member member = principalDetails.getMember();
        LoraRequestResponse response = loraRequestService.create(member, metadata, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    // 내 신청 목록
    @GetMapping("/lora-requests/me")
    public ApiResponse<List<LoraRequestResponse>> listMine(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Member member = principalDetails.getMember();
        return ApiResponse.ok(loraRequestService.listMine(member));
    }

    // 신청 단건 (소유자 또는 관리자)
    @GetMapping("/lora-requests/{requestId}")
    public ApiResponse<LoraRequestResponse> findById(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("requestId") Long requestId) {
        Member member = principalDetails.getMember();
        return ApiResponse.ok(loraRequestService.findById(member, requestId));
    }

    // [관리자] 전체 신청 목록 (status 필터 — 미지정 시 전체)
    @GetMapping("/admin/lora-requests")
    public ApiResponse<List<LoraRequestResponse>> listAll(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(value = "status", required = false) LoraRequestStatus status) {
        Member member = principalDetails.getMember();
        return ApiResponse.ok(loraRequestService.listByStatus(member, status));
    }

    // [관리자] 신청 상태/메모 변경 (Phase 1.5)
    @PatchMapping("/admin/lora-requests/{requestId}")
    public ApiResponse<LoraRequestResponse> updateByAdmin(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("requestId") Long requestId,
            @Valid @RequestBody LoraRequestUpdateRequest request) {
        Member member = principalDetails.getMember();
        return ApiResponse.ok(loraRequestService.updateByAdmin(member, requestId, request));
    }
}
