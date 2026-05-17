package com.wit.ai.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wit.ai.config.ComfyUIProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Component
public class ComfyUIRestClient implements ComfyUIClient {

    private static final String CLIENT_ID = "wit-backend";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ComfyUIRestClient(ComfyUIProperties properties, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(buildRequestFactory(properties))
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes());
                    throw new ComfyUIClientException(
                            "ComfyUI HTTP " + response.getStatusCode() + ": " + body);
                })
                .build();
    }

    @Override
    public String submitWorkflow(String workflowJson) {
        try {
            JsonNode workflowNode = objectMapper.readTree(workflowJson);
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.set("prompt", workflowNode);
            requestBody.put("client_id", CLIENT_ID);

            PromptResponse response = restClient.post()
                    .uri("/prompt")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(PromptResponse.class);

            if (response == null || response.promptId() == null) {
                throw new ComfyUIClientException("ComfyUI /prompt response missing prompt_id");
            }
            if (response.nodeErrors() != null && !response.nodeErrors().isEmpty()) {
                throw new ComfyUIClientException(
                        "ComfyUI workflow node errors: " + response.nodeErrors());
            }
            return response.promptId();
        } catch (IOException e) {
            throw new ComfyUIClientException("Failed to serialize workflow JSON", e);
        }
    }

    @Override
    public Optional<ComfyUIResult> pollResult(String promptId) {
        String responseBody = restClient.get()
                .uri("/history/{id}", promptId)
                .retrieve()
                .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode response = objectMapper.readTree(responseBody);
            if (!response.has(promptId)) {
                return Optional.empty();
            }
            JsonNode outputs = response.get(promptId).get("outputs");
            if (outputs == null || outputs.isEmpty()) {
                return Optional.empty();
            }

            Iterator<Map.Entry<String, JsonNode>> fields = outputs.fields();
            while (fields.hasNext()) {
                JsonNode nodeOutput = fields.next().getValue();
                JsonNode images = nodeOutput.get("images");
                if (images != null && images.isArray() && !images.isEmpty()) {
                    JsonNode first = images.get(0);
                    return Optional.of(new ComfyUIResult(
                            first.get("filename").asText(),
                            first.path("subfolder").asText("")
                    ));
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new ComfyUIClientException("Failed to parse /history response", e);
        }
    }

    @Override
    public byte[] downloadImage(String filename, String subfolder) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/view")
                        .queryParam("filename", filename)
                        .queryParam("subfolder", subfolder)
                        .queryParam("type", "output")
                        .build())
                .retrieve()
                .body(byte[].class);
    }

    private static SimpleClientHttpRequestFactory buildRequestFactory(ComfyUIProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(properties.connectTimeoutSeconds()));
        factory.setReadTimeout(Duration.ofSeconds(properties.readTimeoutSeconds()));
        return factory;
    }

    private record PromptResponse(
            @JsonProperty("prompt_id") String promptId,
            Integer number,
            @JsonProperty("node_errors") Map<String, Object> nodeErrors
    ) {}
}
