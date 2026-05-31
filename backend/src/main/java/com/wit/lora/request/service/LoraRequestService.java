package com.wit.lora.request.service;

import com.wit.global.config.AdminProperties;
import com.wit.lora.domain.LoraCatalog;
import com.wit.lora.repository.LoraCatalogRepository;
import com.wit.lora.request.domain.LoraRequest;
import com.wit.lora.request.domain.LoraRequestStatus;
import com.wit.lora.request.dto.LoraRequestCreateRequest;
import com.wit.lora.request.dto.LoraRequestResponse;
import com.wit.lora.request.dto.LoraRequestUpdateRequest;
import com.wit.lora.request.repository.LoraRequestRepository;
import com.wit.lora.request.storage.LoraImageStorage;
import com.wit.member.domain.Member;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoraRequestService {

    private static final int MIN_IMAGES = 1;
    private static final int MAX_IMAGES = 60;

    private final LoraRequestRepository loraRequestRepository;
    private final LoraImageStorage loraImageStorage;
    private final AdminProperties adminProperties;
    private final LoraCatalogRepository loraCatalogRepository;

    @Transactional
    public LoraRequestResponse create(Member member, LoraRequestCreateRequest metadata,
                                      MultipartFile[] images) {
        int count = (images == null) ? 0 : images.length;
        if (count < MIN_IMAGES || count > MAX_IMAGES) {
            throw new IllegalArgumentException(
                    "이미지는 " + MIN_IMAGES + "~" + MAX_IMAGES + "장이어야 합니다. (현재 " + count + "장)");
        }
        for (MultipartFile image : images) {
            validateImage(image);
        }

        LoraRequest request = loraRequestRepository.save(
                LoraRequest.builder()
                        .member(member)
                        .characterName(metadata.characterName())
                        .triggerWord(metadata.triggerWord())
                        .status(LoraRequestStatus.PENDING)
                        .imageCount(count)
                        .build());

        String imageDir = loraImageStorage.save(request.getRequestId(), images);
        request.updateImageDir(imageDir);

        return LoraRequestResponse.from(request);
    }

    public List<LoraRequestResponse> listMine(Member member) {
        return loraRequestRepository
                .findByMember_MemberIdOrderByCreatedAtDesc(member.getMemberId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public LoraRequestResponse findById(Member member, Long requestId) {
        LoraRequest request = loraRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 LoRA 신청을 찾을 수 없습니다. ID: " + requestId));
        boolean owner = request.getMember().getMemberId().equals(member.getMemberId());
        if (!owner && !adminProperties.isAdmin(member.getEmail())) {
            throw new AccessDeniedException("해당 LoRA 신청에 대한 접근 권한이 없습니다.");
        }
        return toResponse(request);
    }

    // [관리자] status 필터 (null이면 전체)
    public List<LoraRequestResponse> listByStatus(Member member, LoraRequestStatus status) {
        requireAdmin(member);
        List<LoraRequest> list = (status != null)
                ? loraRequestRepository.findByStatusOrderByCreatedAtDesc(status)
                : loraRequestRepository.findAll();
        return list.stream().map(this::toResponse).toList();
    }

    // [관리자] 신청 상태/메모 변경 (Phase 1.5) — 자유 전이, COMPLETED 시 completedAt + 카탈로그(선택) 연결
    @Transactional
    public LoraRequestResponse updateByAdmin(Member member, Long requestId,
                                             LoraRequestUpdateRequest body) {
        requireAdmin(member);
        LoraRequest request = loraRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 LoRA 신청을 찾을 수 없습니다. ID: " + requestId));

        request.updateStatus(body.status());
        if (body.adminNotes() != null) {
            request.updateAdminNotes(body.adminNotes());
        }
        if (body.status() == LoraRequestStatus.COMPLETED) {
            request.markCompleted(LocalDateTime.now());
            if (body.loraCatalogId() != null) {
                LoraCatalog catalog = loraCatalogRepository.findById(body.loraCatalogId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "LoraCatalog을 찾을 수 없습니다. ID: " + body.loraCatalogId()));
                request.linkCatalog(catalog);
            }
        }
        return toResponse(request);
    }

    private LoraRequestResponse toResponse(LoraRequest request) {
        return LoraRequestResponse.from(request, loraImageStorage.listImageUrls(request.getRequestId()));
    }

    private void requireAdmin(Member member) {
        if (!adminProperties.isAdmin(member.getEmail())) {
            throw new AccessDeniedException("관리자만 접근할 수 있습니다.");
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("빈 이미지 파일이 포함되어 있습니다.");
        }
        byte[] head;
        try {
            head = image.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("이미지 읽기 실패", e);
        }
        if (!LoraImageStorage.isSupportedImage(head)) {
            throw new IllegalArgumentException(
                    "이미지 파일이 아닙니다 (PNG/JPEG/WebP만 허용): " + image.getOriginalFilename());
        }
    }
}
