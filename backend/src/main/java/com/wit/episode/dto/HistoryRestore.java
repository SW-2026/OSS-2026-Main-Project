package com.wit.episode.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HistoryRestore {
    private String layoutData;
    private String canvasData;

    // 복원 시 필요한 데이터만 묶어서 반환하는 정적 메서드(만약 canvasData가 필요없다면 삭제 가능)
    public static HistoryRestore of(String layoutData, String canvasData) {
        return new HistoryRestore(layoutData, canvasData);
    }
}
