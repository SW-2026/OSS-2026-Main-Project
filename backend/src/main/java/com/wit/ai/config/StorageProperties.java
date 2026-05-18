package com.wit.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.storage")
public record StorageProperties(
        String localPath
) {
}
