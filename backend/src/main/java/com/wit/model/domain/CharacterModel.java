package com.wit.model.domain;

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
@Table(name = "character_model")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CharacterModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String modelName;                 // 사용자 식별용 (예: "주인공 v1")

    @Column(name = "trigger_word", length = 100)
    private String triggerWord;               // LoRA 트리거 (예: "anya_v1")

    @Column(name = "lora_model_path", length = 500)
    private String loraModelPath;             // 학습 후 채워짐, 생성 시점엔 null

    @Column(name = "appearance_prompt", columnDefinition = "TEXT")
    private String appearancePrompt;

    @Column(name = "outfit_prompt", columnDefinition = "TEXT")
    private String outfitPrompt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModelStatus status;               // PENDING / TRAINING / ACTIVE / FAILED

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public CharacterModel(Project project, String modelName, String triggerWord,
                          String loraModelPath, String appearancePrompt, String outfitPrompt,
                          ModelStatus status) {
        this.project = project;
        this.modelName = modelName;
        this.triggerWord = triggerWord;
        this.loraModelPath = loraModelPath;
        this.appearancePrompt = appearancePrompt;
        this.outfitPrompt = outfitPrompt;
        this.status = status;
    }

    public void markActive() {
        this.status = ModelStatus.ACTIVE;
    }

    public void markFailed() {
        this.status = ModelStatus.FAILED;
    }
}
