package com.wit.ai.storage;

import com.wit.ai.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalImageStorage implements ImageStorage {

    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final StorageProperties properties;

    @Override
    public StoredImage save(byte[] imageBytes, String category, String hint) {
        String dateDir = LocalDate.now().format(DATE_PATTERN);
        String filename = (hint != null && !hint.isBlank()
                ? hint
                : UUID.randomUUID().toString()) + ".png";
        Path target = Paths.get(properties.localPath(), category, dateDir, filename);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("[storage] " + e.getMessage(), e);
        }
        String accessUrl = "/images/" + category + "/" + dateDir + "/" + filename;
        String filePath = target.toAbsolutePath().toString();
        return new StoredImage(filePath, accessUrl);
    }
}
