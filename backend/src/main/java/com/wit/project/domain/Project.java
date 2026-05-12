package com.wit.project.domain;

import com.wit.episode.domain.Episode; // Episode 위치에 따라 수정 필요
import com.wit.member.domain.Member;   // Member 위치에 따라 수정 필요
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity // 필수: JPA 엔티티임을 명시
@Table(name = "project")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // Builder 사용을 위해 모든 필드 생성자 추가
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false) // 관례상 스네이크 케이스(member_id) 사용 권장
    private Member member;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 50)
    private String genre;

    // === AI 이미지 생성용 프롬프트 필드 (추후 CharacterLora 분리 예정) ===

    @Column(columnDefinition = "TEXT")
    private String styleBasePrompt;

    @Column(columnDefinition = "TEXT")
    private String negativePrompt;

    @Column(columnDefinition = "TEXT")
    private String backgroundPrompt;

    @Column(length = 200)
    private String loraTriggerTag;

    @Column(length = 500)
    private String loraModelPath;

    @Column(columnDefinition = "TEXT")
    private String characterAppearancePrompt;

    @Column(columnDefinition = "TEXT")
    private String characterOutfitPrompt;

    // mappedBy에는 Episode 엔티티 안에 정의된 Project 필드 변수명("project")을 적어야 합니다.
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Episode> episodes = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Project(Member member, String title, String genre,
                   String styleBasePrompt, String negativePrompt, String backgroundPrompt,
                   String loraTriggerTag, String loraModelPath,
                   String characterAppearancePrompt, String characterOutfitPrompt) {
        this.member = member;
        this.title = title;
        this.genre = genre;
        this.styleBasePrompt = styleBasePrompt;
        this.negativePrompt = negativePrompt;
        this.backgroundPrompt = backgroundPrompt;
        this.loraTriggerTag = loraTriggerTag;
        this.loraModelPath = loraModelPath;
        this.characterAppearancePrompt = characterAppearancePrompt;
        this.characterOutfitPrompt = characterOutfitPrompt;
    }

    // 부분 업데이트: null이 아닌 값만 덮어씀 — 전 필드 nullable이므로 PATCH 의미로 사용
    public void updatePartial(String title, String genre,
                              String styleBasePrompt, String negativePrompt, String backgroundPrompt,
                              String loraTriggerTag, String loraModelPath,
                              String characterAppearancePrompt, String characterOutfitPrompt) {
        if (title != null) this.title = title;
        if (genre != null) this.genre = genre;
        if (styleBasePrompt != null) this.styleBasePrompt = styleBasePrompt;
        if (negativePrompt != null) this.negativePrompt = negativePrompt;
        if (backgroundPrompt != null) this.backgroundPrompt = backgroundPrompt;
        if (loraTriggerTag != null) this.loraTriggerTag = loraTriggerTag;
        if (loraModelPath != null) this.loraModelPath = loraModelPath;
        if (characterAppearancePrompt != null) this.characterAppearancePrompt = characterAppearancePrompt;
        if (characterOutfitPrompt != null) this.characterOutfitPrompt = characterOutfitPrompt;
    }
}
