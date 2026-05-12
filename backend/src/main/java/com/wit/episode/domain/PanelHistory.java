package com.wit.episode.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "panel_history")
@EntityListeners(AuditingEntityListener.class)
public class PanelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_id", nullable = false)
    private Panel panel;

    @Column(columnDefinition = "LONGTEXT")
    private String layoutData; // 레이어별 위치, 크기 정보 (JSON 문자열)

    @Column(nullable = false)
    private int version;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String canvasData; // Fabric.js의 JSON 데이터

    @CreatedDate
    @Column(updatable = false, name = "saved_at")
    private LocalDateTime savedAt;

    @Builder
    public PanelHistory(Panel panel, int version, String layoutData, String canvasData) {
        this.panel = panel;
        this.version = version;
        this.layoutData = layoutData;
        this.canvasData = canvasData;
    }
}