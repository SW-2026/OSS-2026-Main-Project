package com.wit.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wit.ai.config.LlmProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiClientTest {

    private MockWebServer mockServer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    private GeminiClient buildClient(String apiKey) {
        String baseUrl = "http://" + mockServer.getHostName() + ":" + mockServer.getPort();
        LlmProperties props = new LlmProperties(
                "gemini", baseUrl, apiKey, "gemini-2.5-flash", 2000, 0.7
        );
        return new GeminiClient(props, objectMapper);
    }

    @Test
    void complete_returns_text_on_success() {
        String responseBody = """
                {
                  "candidates": [
                    {"content": {"parts": [{"text": "Hello from Gemini"}]}}
                  ]
                }
                """;
        mockServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        GeminiClient client = buildClient("test-key");
        String result = client.complete("you are a helpful assistant", "say hello");

        assertThat(result).isEqualTo("Hello from Gemini");
    }

    @Test
    void complete_400_throws_LlmException() {
        mockServer.enqueue(new MockResponse().setResponseCode(400).setBody("Bad request"));
        GeminiClient client = buildClient("test-key");
        assertThatThrownBy(() -> client.complete("sys", "msg"))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("HTTP");
    }

    @Test
    void complete_403_throws_LlmException() {
        mockServer.enqueue(new MockResponse().setResponseCode(403).setBody("Forbidden"));
        GeminiClient client = buildClient("test-key");
        assertThatThrownBy(() -> client.complete("sys", "msg"))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("HTTP");
    }

    @Test
    void complete_429_throws_LlmException() {
        mockServer.enqueue(new MockResponse().setResponseCode(429).setBody("Rate limit"));
        GeminiClient client = buildClient("test-key");
        assertThatThrownBy(() -> client.complete("sys", "msg"))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("HTTP");
    }

    @Test
    void complete_empty_api_key_throws_LlmException() {
        GeminiClient client = buildClient("");
        assertThatThrownBy(() -> client.complete("sys", "msg"))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("API key");
    }

    @Test
    void complete_empty_candidates_throws_LlmException() {
        mockServer.enqueue(new MockResponse()
                .setBody("{\"candidates\": []}")
                .addHeader("Content-Type", "application/json"));

        GeminiClient client = buildClient("test-key");
        assertThatThrownBy(() -> client.complete("sys", "msg"))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("no candidates");
    }
}
