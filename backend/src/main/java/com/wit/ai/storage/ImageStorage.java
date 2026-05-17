package com.wit.ai.storage;

public interface ImageStorage {

    /**
     * 이미지 byte[]를 저장하고 접근 정보를 반환.
     *
     * @param imageBytes 원본 이미지 (PNG)
     * @param category   "character" / "background" / "panel" / "compose"
     * @param hint       파일명 힌트 (nullable). null이면 UUID 사용.
     * @return 저장된 파일의 경로 + 접근 URL
     */
    StoredImage save(byte[] imageBytes, String category, String hint);
}
