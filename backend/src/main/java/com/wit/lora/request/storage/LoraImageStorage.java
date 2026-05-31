package com.wit.lora.request.storage;

import com.wit.ai.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// LoRA 신청 이미지 저장 — 요청별 폴더, 클라 파일명 미사용({index}.{ext}). StorageProperties 재사용.
@Component
@RequiredArgsConstructor
public class LoraImageStorage {

    private static final String CATEGORY = "lora-request";

    private final StorageProperties properties;

    // {localPath}/lora-request/{requestId}/{i}.{ext} 로 저장하고 접근 URL prefix 반환
    public String save(Long requestId, MultipartFile[] images) {
        Path dir = Paths.get(properties.localPath(), CATEGORY, String.valueOf(requestId));
        try {
            Files.createDirectories(dir);
            for (int i = 0; i < images.length; i++) {
                byte[] bytes = images[i].getBytes();
                String ext = extensionOf(bytes);
                Files.write(dir.resolve(i + "." + ext), bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException("[lora-storage] " + e.getMessage(), e);
        }
        return "/images/" + CATEGORY + "/" + requestId + "/";
    }

    // === 매직바이트 기반 이미지 판별 (클라 content-type 신뢰 X) ===

    public static boolean isSupportedImage(byte[] b) {
        return isPng(b) || isJpeg(b) || isWebp(b);
    }

    public static String extensionOf(byte[] b) {
        if (isPng(b)) return "png";
        if (isJpeg(b)) return "jpg";
        if (isWebp(b)) return "webp";
        return "bin";
    }

    private static boolean isPng(byte[] b) {
        // 89 50 4E 47
        return b.length >= 4
                && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
    }

    private static boolean isJpeg(byte[] b) {
        // FF D8 FF
        return b.length >= 3
                && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }

    private static boolean isWebp(byte[] b) {
        // "RIFF" .... "WEBP"
        return b.length >= 12
                && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }
}
