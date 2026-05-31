package com.wit.lora.request.domain;

import com.wit.lora.domain.LoraCatalog;
import com.wit.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lora_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoraRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "character_name", nullable = false, length = 100)
    private String characterName;

    @Column(name = "trigger_word", nullable = false, length = 1000)
    private String triggerWord;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoraRequestStatus status;

    @Column(name = "image_count", nullable = false)
    private int imageCount;

    @Column(name = "image_dir", length = 500)
    private String imageDir;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // 완료(COMPLETED) 시 생성된 LoRA 카탈로그 연결 — 신청 시점엔 null (Phase 1.5/2)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lora_catalog_id")
    private LoraCatalog loraCatalog;

    @Builder
    public LoraRequest(Member member, String characterName, String triggerWord,
                       LoraRequestStatus status, int imageCount) {
        this.member = member;
        this.characterName = characterName;
        this.triggerWord = triggerWord;
        this.status = status;
        this.imageCount = imageCount;
    }

    // 이미지 저장 후 디렉토리 경로 기록
    public void updateImageDir(String imageDir) {
        this.imageDir = imageDir;
    }
}
