package com.wit.model.controller;

import com.wit.auth.dto.PrincipalDetails;
import com.wit.global.response.ApiResponse;
import com.wit.member.domain.Member;
import com.wit.model.dto.CharacterModelCreateRequest;
import com.wit.model.dto.CharacterModelDetailResponse;
import com.wit.model.dto.CharacterModelStatusResponse;
import com.wit.model.dto.CharacterModelSummaryResponse;
import com.wit.model.service.CharacterModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CharacterModelController {

    private final CharacterModelService characterModelService;

    /**
     * 1. 모델 생성 (POST /api/projects/{projectId}/models, multipart)
     * - metadata: CharacterModelCreateRequest (JSON part)
     * - images:   레퍼런스 이미지 다수 (1.5단계는 받기만 하고 mock 처리)
     */
    @PostMapping(value = "/api/projects/{projectId}/models",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CharacterModelDetailResponse> create(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long projectId,
            @Valid @RequestPart("metadata") CharacterModelCreateRequest metadata,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        Member member = principalDetails.getMember();
        CharacterModelDetailResponse response =
                characterModelService.create(member, projectId, metadata, images);
        return ApiResponse.created(response);
    }

    /**
     * 2. Project의 모델 목록 조회 (GET /api/projects/{projectId}/models)
     */
    @GetMapping("/api/projects/{projectId}/models")
    public ApiResponse<List<CharacterModelSummaryResponse>> getModelsByProject(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long projectId
    ) {
        Member member = principalDetails.getMember();
        return ApiResponse.ok(characterModelService.findAllByProject(member, projectId));
    }

    /**
     * 3. 모델 단건 상세 조회 (GET /api/models/{modelId})
     */
    @GetMapping("/api/models/{modelId}")
    public ApiResponse<CharacterModelDetailResponse> getOne(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long modelId
    ) {
        Member member = principalDetails.getMember();
        return ApiResponse.ok(characterModelService.findById(member, modelId));
    }

    /**
     * 4. 학습 트리거 (POST /api/models/{modelId}/train)
     * - 현재 mock: 즉시 status=ACTIVE로 변경
     * - 실제 LoRA 학습은 N단계
     */
    @PostMapping("/api/models/{modelId}/train")
    public ApiResponse<CharacterModelStatusResponse> train(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long modelId
    ) {
        Member member = principalDetails.getMember();
        return ApiResponse.ok(characterModelService.train(member, modelId));
    }

    /**
     * 5. LoRA 카탈로그 기반 자동 등록 (POST /api/projects/{projectId}/models/from-lora?loraFileName=...)
     * - 이미지/모델명 입력 없이 한 번에 등록 (소재 탭 카드 클릭 흐름)
     * - 멱등 — 같은 LoRA로 재호출 시 기존 모델 반환
     */
    @PostMapping("/api/projects/{projectId}/models/from-lora")
    public ApiResponse<CharacterModelDetailResponse> createFromLora(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long projectId,
            @RequestParam String loraFileName
    ) {
        Member member = principalDetails.getMember();
        return ApiResponse.created(
                characterModelService.createFromLora(member, projectId, loraFileName)
        );
    }
}
