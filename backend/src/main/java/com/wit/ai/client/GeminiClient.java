package com.wit.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wit.ai.config.LlmProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Component
public class GeminiClient implements LlmClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final LlmProperties properties;

    public GeminiClient(LlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes());
                    throw new LlmException(
                            "Gemini HTTP " + response.getStatusCode() + ": " + body);
                })
                .build();
    }

    @Override
    public String complete(String systemPrompt, String userMessage) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new LlmException("Gemini API key is not configured (ai.llm.api-key)");
        }
        try {
            ObjectNode body = buildRequestBody(systemPrompt, userMessage);
            String json = objectMapper.writeValueAsString(body);
            String responseBody = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/{model}:generateContent")
                            .queryParam("key", properties.apiKey())
                            .build(properties.model()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                throw new LlmException("Gemini response body is empty");
            }
            return extractText(responseBody);
        } catch (IOException e) {
            throw new LlmException("Failed to process Gemini request/response", e);
        }
    }

    private ObjectNode buildRequestBody(String systemPrompt, String userMessage) {
        ObjectNode root = objectMapper.createObjectNode();

        // system_instruction
        ObjectNode systemInstruction = objectMapper.createObjectNode();
        ArrayNode systemParts = objectMapper.createArrayNode();
        systemParts.add(objectMapper.createObjectNode().put("text", systemPrompt));
        systemInstruction.set("parts", systemParts);
        root.set("system_instruction", systemInstruction);

        // contents (user message)
        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode userContent = objectMapper.createObjectNode();
        userContent.put("role", "user");
        ArrayNode userParts = objectMapper.createArrayNode();
        userParts.add(objectMapper.createObjectNode().put("text", userMessage));
        userContent.set("parts", userParts);
        contents.add(userContent);
        root.set("contents", contents);

        // generationConfig
        ObjectNode config = objectMapper.createObjectNode();
        config.put("temperature", properties.temperature());
        config.put("maxOutputTokens", properties.maxTokens());
        config.put("responseMimeType", "application/json");
        root.set("generationConfig", config);

        return root;
    }

    private String extractText(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.get("candidates");
        if (candidates == null || !candidates.isArray() || candidates.isEmpty()) {
            throw new LlmException("Gemini response has no candidates: " + responseBody);
        }
        JsonNode content = candidates.get(0).get("content");
        if (content == null) {
            throw new LlmException("Gemini candidate has no content: " + responseBody);
        }
        JsonNode parts = content.get("parts");
        if (parts == null || !parts.isArray() || parts.isEmpty()) {
            throw new LlmException("Gemini content has no parts: " + responseBody);
        }
        JsonNode firstPart = parts.get(0);
        if (firstPart.get("text") == null) {
            throw new LlmException("Gemini part has no text: " + responseBody);
        }
        return firstPart.get("text").asText();
    }
}
