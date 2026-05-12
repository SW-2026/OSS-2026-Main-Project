package com.wit.episode.domain;

import com.wit.episode.domain.Panel;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "panel_history")
public class PanelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panelId", nullable = false)
    private Panel panel;

    @Column(nullable = false)
    private int version;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String canvasData; // Fabric.js의 JSON 데이터

    private LocalDateTime createdAt;

    @Builder
    public PanelHistory(Panel panel, int version, String canvasData) {
        this.panel = panel;
        this.version = version;
        this.canvasData = canvasData;
        this.createdAt = LocalDateTime.now();
    }
}