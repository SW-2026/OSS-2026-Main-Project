package com.wit.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.llm")
public record LlmProperties(
        String provider,
        String baseUrl,
        String apiKey,
        String model,
        Integer maxTokens,
        Double temperature
) {
}
