package com.wit.ai.storage;

import com.wit.ai.config.StorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class LocalImageStorageTest {

    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private LocalImageStorage build(Path tempDir) {
        return new LocalImageStorage(new StorageProperties(tempDir.toString()));
    }

    @Test
    void save_writes_file_to_disk_and_returns_url(@TempDir Path tempDir) throws IOException {
        LocalImageStorage storage = build(tempDir);
        byte[] bytes = new byte[]{1, 2, 3};

        StoredImage result = storage.save(bytes, "panel", null);

        assertThat(result.accessUrl()).matches("^/images/panel/\\d{4}-\\d{2}-\\d{2}/.+\\.png$");
        Path target = Path.of(result.filePath());
        assertThat(Files.exists(target)).isTrue();
        assertThat(Files.readAllBytes(target)).containsExactly(1, 2, 3);
    }

    @Test
    void save_creates_parent_directories_if_missing(@TempDir Path tempDir) {
        LocalImageStorage storage = build(tempDir);
        String today = LocalDate.now().format(DATE_PATTERN);

        storage.save(new byte[]{0}, "character", null);

        assertThat(Files.exists(tempDir.resolve("character").resolve(today))).isTrue();
    }

    @Test
    void save_generates_unique_filenames_for_null_hint(@TempDir Path tempDir) {
        LocalImageStorage storage = build(tempDir);

        StoredImage r1 = storage.save(new byte[]{1}, "panel", null);
        StoredImage r2 = storage.save(new byte[]{2}, "panel", null);

        assertThat(r1.accessUrl()).isNotEqualTo(r2.accessUrl());
        assertThat(r1.filePath()).isNotEqualTo(r2.filePath());
    }

    @Test
    void save_uses_hint_as_filename_when_provided(@TempDir Path tempDir) {
        LocalImageStorage storage = build(tempDir);

        StoredImage result = storage.save(new byte[]{1}, "panel", "custom-name");

        assertThat(result.accessUrl()).endsWith("/custom-name.png");
    }
}
