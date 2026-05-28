package com.wit.lora.config;

import com.wit.lora.domain.LoraCatalog;
import com.wit.lora.repository.LoraCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoraCatalogSeedRunner implements CommandLineRunner {

    private final LoraCatalogProperties properties;
    private final LoraCatalogRepository repository;

    // 부팅 시 yml(ai.lora-catalog.items) → DB upsert. fileName 기준 멱등.
    @Override
    @Transactional
    public void run(String... args) {
        if (properties.getItems() == null || properties.getItems().isEmpty()) {
            log.info("[LoraCatalogSeed] ai.lora-catalog.items 비어있음 — seed skip");
            return;
        }
        int inserted = 0;
        int updated = 0;
        for (LoraCatalogProperties.Entry e : properties.getItems()) {
            LoraCatalog existing = repository.findByFileName(e.getFileName()).orElse(null);
            if (existing == null) {
                repository.save(LoraCatalog.builder()
                        .fileName(e.getFileName())
                        .displayName(e.getDisplayName())
                        .thumbnailUrl(e.getThumbnailUrl())
                        .triggerWord(e.getTriggerWord())
                        .description(e.getDescription())
                        .build());
                inserted++;
            } else {
                existing.update(e.getDisplayName(), e.getThumbnailUrl(),
                                e.getTriggerWord(), e.getDescription());
                updated++;
            }
        }
        log.info("[LoraCatalogSeed] 완료 — inserted={}, updated={}, total={}",
                inserted, updated, properties.getItems().size());
    }
}
