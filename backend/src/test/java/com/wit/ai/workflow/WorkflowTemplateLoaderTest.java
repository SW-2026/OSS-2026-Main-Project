package com.wit.ai.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowTemplateLoaderTest {

    private WorkflowTemplateLoader loader;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        loader = new WorkflowTemplateLoader(objectMapper);
    }

    @Test
    void character_json_all_params_injected_correctly() throws Exception {
        WorkflowParams params = new WorkflowParams(
                "1girl, running, scared expression",
                "(worst quality:1.4), bad anatomy",
                12345L,
                "anya_v1"
        );

        String result = loader.load("character.json", params);
        JsonNode root = objectMapper.readTree(result);

        // KSampler seed 갱신
        assertThat(findByClassType(root, "KSampler").get("inputs").get("seed").asLong())
                .isEqualTo(12345L);

        // StringConcatenate.string_b 갱신 (positive)
        assertThat(findByClassType(root, "StringConcatenate")
                .get("inputs").get("string_b").asText())
                .isEqualTo("1girl, running, scared expression");

        // negative CLIPTextEncode.text 갱신 (KSampler.negative[0] 추적)
        String negativeNodeId = findByClassType(root, "KSampler")
                .get("inputs").get("negative").get(0).asText();
        assertThat(root.get(negativeNodeId).get("inputs").get("text").asText())
                .isEqualTo("(worst quality:1.4), bad anatomy");

        // LoraTagLoader.text 갱신
        assertThat(findByClassType(root, "LoraTagLoader").get("inputs").get("text").asText())
                .isEqualTo("anya_v1");
    }

    @Test
    void lora_name_null_preserves_original_lora_text() throws Exception {
        // 원본 LoraTagLoader.text 추출 — character.json 수정에 영향 안 받게
        String originalLoraText = loadOriginalLoraTagText();

        WorkflowParams params = new WorkflowParams(
                "test prompt",
                "negative",
                42L,
                null
        );
        String result = loader.load("character.json", params);
        JsonNode root = objectMapper.readTree(result);

        assertThat(findByClassType(root, "LoraTagLoader").get("inputs").get("text").asText())
                .isEqualTo(originalLoraText);
    }

    private JsonNode findByClassType(JsonNode root, String classType) {
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            if (classType.equals(entry.getValue().path("class_type").asText())) {
                return entry.getValue();
            }
        }
        throw new IllegalStateException("Node not found: " + classType);
    }

    private String loadOriginalLoraTagText() throws IOException {
        try (var input = new ClassPathResource("comfyui/workflows/character.json")
                .getInputStream()) {
            JsonNode root = objectMapper.readTree(input);
            return findByClassType(root, "LoraTagLoader").get("inputs").get("text").asText();
        }
    }
}
