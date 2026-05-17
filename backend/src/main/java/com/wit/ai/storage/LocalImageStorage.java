package com.wit.ai.storage;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LocalImageStorage implements ImageStorage {

    // 2-13에서 본격 구현: 실제 디스크 저장 + ai.storage.local-path 사용.
    // 본 단계는 컴파일/DI를 위한 placeholder — URL만 생성하고 byte[]는 버림.
    @Override
    public StoredImage save(byte[] imageBytes, String category, String hint) {
        String filename = (hint != null && !hint.isBlank() ? hint : UUID.randomUUID().toString()) + ".png";
        String filePath = "placeholder/" + category + "/" + filename;
        String accessUrl = "/images/" + category + "/" + filename;
        return new StoredImage(filePath, accessUrl);
    }
}
