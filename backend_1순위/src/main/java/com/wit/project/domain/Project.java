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

    // mappedBy에는 Episode 엔티티 안에 정의된 Project 필드 변수명("project")을 적어야 합니다.
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Episode> episodes = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Project(Member member, String title, String genre) {
        this.member = member;
        this.title = title;
        this.genre = genre;
    }
}