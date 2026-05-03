package com.wit.episode.domain;

import com.wit.project.domain.Project;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "episode")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long episodeId; // 회차 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId", nullable = false)
    private Project project; // 프로젝트 식별자 (FK)

    @Column(nullable = false)
    private Integer epNumber; // 회차 번호 (1화, 2화 등)

    @Column(length = 200)
    private String epTitle; // 회차 제목

    @Column(columnDefinition = "TEXT")
    private String content; // 회차 본문 내용 (설계안엔 없으나 서비스 구현을 위해 추가)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성일시

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt; // 수정일시

    @Builder
    public Episode(Project project, Integer epNumber, String epTitle, String content) {
        this.project = project;
        this.epNumber = epNumber;
        this.epTitle = epTitle;
        this.content = content;
    }
}
