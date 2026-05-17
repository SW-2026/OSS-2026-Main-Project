package com.wit.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wit.ai.client.LlmClient;
import com.wit.ai.client.LlmException;
import com.wit.ai.dto.CharacterMention;
import com.wit.ai.dto.ScenarioPanel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ScenarioAnalyzer {

    private static final int EXPECTED_PANEL_COUNT = 10;
    private static final int MAX_RETRIES = 1;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;

    public ScenarioAnalyzer(
            LlmClient llmClient,
            ObjectMapper objectMapper,
            @Value("classpath:prompts/scenario-analyzer.txt") Resource promptResource
    ) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
        try {
            this.systemPrompt = promptResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load scenario-analyzer prompt", e);
        }
    }

    public List<ScenarioPanel> analyze(String scenarioText, List<CharacterMention> mentions) {
        String userMessage = buildUserMessage(scenarioText, mentions);
        Set<Long> validModelIds = collectValidModelIds(mentions);

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            String response = llmClient.complete(systemPrompt, userMessage);
            List<ScenarioPanel> panels = parsePanels(response);
            if (panels.size() == EXPECTED_PANEL_COUNT) {
                return sanitize(panels, validModelIds);
            }
        }
        throw new LlmException(
                "ScenarioAnalyzer failed: expected " + EXPECTED_PANEL_COUNT
                        + " panels after " + (MAX_RETRIES + 1) + " attempts");
    }

    private String buildUserMessage(String scenarioText, List<CharacterMention> mentions) {
        StringBuilder sb = new StringBuilder();
        sb.append("[등장 캐릭터 매칭 (프론트 결정)]\n");
        if (mentions != null) {
            for (CharacterMention m : mentions) {
                sb.append("- @").append(m.name())
                        .append(" → modelId=").append(m.modelId())
                        .append(" (triggerWord: ").append(m.triggerWord()).append(")\n");
            }
        }
        sb.append("\n[이야기]\n").append(scenarioText);
        return sb.toString();
    }

    private List<ScenarioPanel> parsePanels(String response) {
        try {
            JsonNode root = objectMapper.readTree(stripCodeFence(response));
            JsonNode panelsNode = root.get("panels");
            if (panelsNode == null || !panelsNode.isArray()) {
                return List.of();
            }
            return objectMapper.convertValue(panelsNode, new TypeReference<List<ScenarioPanel>>() {
            });
        } catch (IOException e) {
            return List.of();
        }
    }

    private String stripCodeFence(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    private Set<Long> collectValidModelIds(List<CharacterMention> mentions) {
        Set<Long> set = new HashSet<>();
        if (mentions != null) {
            for (CharacterMention m : mentions) {
                if (m.modelId() != null) {
                    set.add(m.modelId());
                }
            }
        }
        return set;
    }

    private List<ScenarioPanel> sanitize(List<ScenarioPanel> panels, Set<Long> validModelIds) {
        List<ScenarioPanel> result = new ArrayList<>(panels.size());
        for (ScenarioPanel p : panels) {
            Long modelId = p.characterModelId();
            if (modelId != null && !validModelIds.contains(modelId)) {
                modelId = null;
            }
            result.add(new ScenarioPanel(
                    p.panelOrder(),
                    p.panelScenario(),
                    modelId,
                    p.actionTags(),
                    p.emotionTags(),
                    p.poseTags(),
                    p.backgroundTags(),
                    p.cameraTags()));
        }
        return result;
    }
}
