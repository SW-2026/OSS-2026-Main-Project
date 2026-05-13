package com.wit.episode.dto;

import com.wit.episode.domain.PanelHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HistoryResponse {
    private int version;
    private LocalDateTime savedAt;

    // Entity -> DTO 변환 정적 팩토리 메서드
    public static HistoryResponse from(PanelHistory history) {
        return new HistoryResponse(
                history.getVersion(),
                history.getSavedAt()
        );
    }
}
