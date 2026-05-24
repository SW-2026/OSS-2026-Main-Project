package com.wit.episode.domain;

import com.wit.project.domain.Project;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "episode")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long episodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private Integer epNumber;

    @Column(length = 200)
    private String epTitle;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 추가: 에피소드에 속한 패널(컷) 리스트
    // CascadeType.ALL을 통해 에피소드 저장/삭제 시 패널도 함께 관리됩니다.
    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Panel> panels = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Episode(Project project, Integer epNumber, String epTitle, String content) {
        this.project = project;
        this.epNumber = epNumber;
        this.epTitle = epTitle;
        this.content = content;
    }

    // 편의 메서드: 에피소드에 패널을 추가할 때 연관관계를 자동으로 설정
    public void addPanel(Panel panel) {
        this.panels.add(panel);
        panel.setEpisode(this);
    }

    // PATCH — null이 아닌 필드만 덮어씀 (dirty checking으로 자동 저장)
    // Project.updatePartial 패턴 그대로
    public void updatePartial(Integer epNumber, String epTitle) {
        if (epNumber != null) {
            this.epNumber = epNumber;
        }
        if (epTitle != null) {
            this.epTitle = epTitle;
        }
    }
}