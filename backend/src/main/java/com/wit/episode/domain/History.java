package com.wit.episode.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class History extends BaseTimeEntity { // 생성시간 관리를 위해 BaseTimeEntity 상속 추천

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panelId")
    private Panel panel;

    @Column(columnDefinition = "LONGTEXT") // JSON 데이터는 양이 많을 수 있으므로 LONGTEXT 권장
    private String layoutData;

    private Integer version;

    @Builder
    public History(Panel panel, String layoutData, Integer version) {
        this.panel = panel;
        this.layoutData = layoutData;
        this.version = version;
    }
}
