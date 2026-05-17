package com.wit.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wit.ai.client.LlmClient;
import com.wit.ai.client.LlmException;
import com.wit.ai.dto.CharacterMention;
import com.wit.ai.dto.ScenarioPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScenarioAnalyzerTest {

    private ObjectMapper objectMapper;
    private ByteArrayResource promptResource;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        promptResource = new ByteArrayResource("system prompt".getBytes());
    }

    private ScenarioAnalyzer build(LlmClient client) {
        return new ScenarioAnalyzer(client, objectMapper, promptResource);
    }

    private String tenPanelsJson(Long firstModelId) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"panels\":[");
        for (int i = 1; i <= 10; i++) {
            if (i > 1) sb.append(",");
            Long modelId = (i == 1) ? firstModelId : 2L;
            sb.append("{")
                    .append("\"panelOrder\":").append(i).append(",")
                    .append("\"panelScenario\":\"컷 ").append(i).append("\",")
                    .append("\"characterModelId\":").append(modelId).append(",")
                    .append("\"actionTags\":\"running\",")
                    .append("\"emotionTags\":\"scared\",")
                    .append("\"poseTags\":\"dynamic\",")
                    .append("\"backgroundTags\":\"alley\",")
                    .append("\"cameraTags\":\"wide angle\"")
                    .append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    @Test
    void analyze_returns_ten_panels_on_success() {
        LlmClient client = (sys, user) -> tenPanelsJson(1L);
        ScenarioAnalyzer analyzer = build(client);

        List<ScenarioPanel> panels = analyzer.analyze(
                "story",
                List.of(
                        new CharacterMention("연우", 1L, "yeonwoo_v1"),
                        new CharacterMention("지섭", 2L, "jiseop_v1")));

        assertThat(panels).hasSize(10);
        assertThat(panels.get(0).panelOrder()).isEqualTo(1);
        assertThat(panels.get(0).characterModelId()).isEqualTo(1L);
        assertThat(panels.get(9).panelOrder()).isEqualTo(10);
    }

    @Test
    void analyze_retries_once_when_size_mismatch() {
        AtomicInteger calls = new AtomicInteger(0);
        LlmClient client = (sys, user) -> {
            int n = calls.incrementAndGet();
            return (n == 1) ? "{\"panels\":[]}" : tenPanelsJson(1L);
        };
        ScenarioAnalyzer analyzer = build(client);

        List<ScenarioPanel> panels = analyzer.analyze(
                "story", List.of(new CharacterMention("연우", 1L, "yeonwoo_v1")));

        assertThat(panels).hasSize(10);
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void analyze_throws_when_retry_also_fails() {
        AtomicInteger calls = new AtomicInteger(0);
        LlmClient client = (sys, user) -> {
            calls.incrementAndGet();
            return "{\"panels\":[]}";
        };
        ScenarioAnalyzer analyzer = build(client);

        assertThatThrownBy(() -> analyzer.analyze("story", List.of()))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("10");
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void analyze_forces_null_when_modelId_out_of_range() {
        LlmClient client = (sys, user) -> tenPanelsJson(999L);
        ScenarioAnalyzer analyzer = build(client);

        List<ScenarioPanel> panels = analyzer.analyze(
                "story",
                List.of(
                        new CharacterMention("연우", 1L, "yeonwoo_v1"),
                        new CharacterMention("지섭", 2L, "jiseop_v1")));

        assertThat(panels.get(0).characterModelId()).isNull();
        assertThat(panels.get(1).characterModelId()).isEqualTo(2L);
    }

    @Test
    void analyze_handles_code_fence_response() {
        String wrapped = "```json\n" + tenPanelsJson(1L) + "\n```";
        LlmClient client = (sys, user) -> wrapped;
        ScenarioAnalyzer analyzer = build(client);

        List<ScenarioPanel> panels = analyzer.analyze(
                "story", List.of(new CharacterMention("연우", 1L, "yeonwoo_v1")));

        assertThat(panels).hasSize(10);
        assertThat(panels.get(0).characterModelId()).isEqualTo(1L);
    }
}
