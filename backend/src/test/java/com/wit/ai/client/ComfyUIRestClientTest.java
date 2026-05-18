package com.wit.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wit.ai.config.ComfyUIProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ComfyUIRestClientTest {

    private MockWebServer mockServer;
    private ComfyUIRestClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        String baseUrl = "http://" + mockServer.getHostName() + ":" + mockServer.getPort();
        ComfyUIProperties props = new ComfyUIProperties(baseUrl, 5, 60, 0, 0);
        client = new ComfyUIRestClient(props, new ObjectMapper());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void submitWorkflow_returns_promptId_on_success() {
        mockServer.enqueue(new MockResponse()
                .setBody("{\"prompt_id\":\"abc-123\",\"number\":0,\"node_errors\":{}}")
                .addHeader("Content-Type", "application/json"));

        String promptId = client.submitWorkflow("{\"3\":{\"class_type\":\"KSampler\"}}");

        assertThat(promptId).isEqualTo("abc-123");
    }

    @Test
    void submitWorkflow_4xx_throws_ComfyUIClientException() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("Bad workflow"));

        assertThatThrownBy(() -> client.submitWorkflow("{}"))
                .isInstanceOf(ComfyUIClientException.class)
                .hasMessageContaining("HTTP");
    }

    @Test
    void submitWorkflow_node_errors_throws_ComfyUIClientException() {
        mockServer.enqueue(new MockResponse()
                .setBody("{\"prompt_id\":\"abc\",\"node_errors\":{\"3\":\"missing input\"}}")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.submitWorkflow("{}"))
                .isInstanceOf(ComfyUIClientException.class)
                .hasMessageContaining("node errors");
    }

    @Test
    void pollResult_returns_empty_when_pending() {
        mockServer.enqueue(new MockResponse()
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));

        Optional<ComfyUIResult> result = client.pollResult("abc-123");

        assertThat(result).isEmpty();
    }

    @Test
    void pollResult_returns_filename_when_completed() {
        String body = """
                {
                  "abc-123": {
                    "outputs": {
                      "9": {
                        "images": [
                          {"filename": "ComfyUI_00001_.png", "subfolder": "", "type": "output"}
                        ]
                      }
                    }
                  }
                }
                """;
        mockServer.enqueue(new MockResponse()
                .setBody(body)
                .addHeader("Content-Type", "application/json"));

        Optional<ComfyUIResult> result = client.pollResult("abc-123");

        assertThat(result).isPresent();
        assertThat(result.get().filename()).isEqualTo("ComfyUI_00001_.png");
        assertThat(result.get().subfolder()).isEmpty();
    }

    @Test
    void downloadImage_returns_bytes() {
        byte[] imageBytes = {0x12, 0x34, 0x56, 0x78};
        mockServer.enqueue(new MockResponse()
                .setBody(new Buffer().write(imageBytes))
                .addHeader("Content-Type", "image/png"));

        byte[] result = client.downloadImage("test.png", "");

        assertThat(result).isEqualTo(imageBytes);
    }
}
