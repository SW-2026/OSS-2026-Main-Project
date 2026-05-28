package com.wit.lora.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lora_catalog")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoraCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, unique = true, length = 500)
    private String fileName;            // ComfyUI 파일명 (확장자 제외)

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;         // 접속자 표시용

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;        // /images/lora-thumbs/... 등

    @Column(name = "trigger_word", length = 1000)
    private String triggerWord;         // 선택, character_model.triggerWord와 같은 길이 정책

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public LoraCatalog(String fileName, String displayName, String thumbnailUrl,
                       String triggerWord, String description) {
        this.fileName = fileName;
        this.displayName = displayName;
        this.thumbnailUrl = thumbnailUrl;
        this.triggerWord = triggerWord;
        this.description = description;
    }

    public void update(String displayName, String thumbnailUrl, String triggerWord, String description) {
        this.displayName = displayName;
        this.thumbnailUrl = thumbnailUrl;
        this.triggerWord = triggerWord;
        this.description = description;
    }
}
