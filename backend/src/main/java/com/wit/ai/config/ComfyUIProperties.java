package com.wit.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.comfyui")
public record ComfyUIProperties(
        String baseUrl,
        int connectTimeoutSeconds,
        int readTimeoutSeconds,
        int pollIntervalMillis,
        int maxPollAttempts
) {
}
