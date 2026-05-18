package com.wit.ai.domain;

import com.wit.model.domain.CharacterModel;
import com.wit.project.domain.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "character_asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CharacterAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_model_id", nullable = false)
    private CharacterModel characterModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;                  // 로컬 디스크 접근 URL

    @Column(name = "final_prompt", columnDefinition = "TEXT")
    private String finalPrompt;               // 재현/디버깅용 (nullable)

    @Column(name = "seed")
    private Long seed;                        // 재현용 (nullable)

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public CharacterAsset(CharacterModel characterModel, Project project,
                          String imageUrl, String finalPrompt, Long seed) {
        this.characterModel = characterModel;
        this.project = project;
        this.imageUrl = imageUrl;
        this.finalPrompt = finalPrompt;
        this.seed = seed;
    }
}
