package com.wit.lora.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "ai.lora-catalog")
public class LoraCatalogProperties {

    private List<Entry> items = new ArrayList<>();

    @Getter
    @Setter
    public static class Entry {
        private String fileName;
        private String displayName;
        private String thumbnailUrl;
        private String triggerWord;
        private String description;
    }
}
