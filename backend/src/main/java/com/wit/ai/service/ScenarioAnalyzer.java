package com.wit.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wit.ai.client.LlmClient;
import com.wit.ai.client.LlmException;
import com.wit.ai.dto.BackgroundMention;
import com.wit.ai.dto.CharacterMention;
import com.wit.ai.dto.ScenarioPanel;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ScenarioAnalyzer {

    private static final int MIN_PANEL_COUNT = 6;
    private static final int MAX_PANEL_COUNT = 14;
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

    public List<ScenarioPanel> analyze(String scenarioText,
                                       List<CharacterMention> mentions,
                                       List<BackgroundMention> backgrounds) {
        String userMessage = buildUserMessage(scenarioText, mentions, backgrounds);
        Set<Long> validModelIds = collectValidModelIds(mentions);
        Set<Long> validAssetIds = collectValidAssetIds(backgrounds);

        String lastSnippet = null;
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            String response = llmClient.complete(systemPrompt, userMessage);
            lastSnippet = snippet(response);
            List<ScenarioPanel> panels = parsePanels(response);
            int size = panels.size();
            if (size >= MIN_PANEL_COUNT && size <= MAX_PANEL_COUNT) {
                return sanitize(panels, validModelIds, validAssetIds);
            }
            log.warn("ScenarioAnalyzer attempt {}: got {} panels (expected {}~{})",
                    attempt + 1, size, MIN_PANEL_COUNT, MAX_PANEL_COUNT);
        }
        throw new LlmException(
                "ScenarioAnalyzer failed: expected " + MIN_PANEL_COUNT + "~" + MAX_PANEL_COUNT
                        + " panels after " + (MAX_RETRIES + 1) + " attempts"
                        + ". last response snippet: " + lastSnippet);
    }

    private String buildUserMessage(String scenarioText,
                                    List<CharacterMention> mentions,
                                    List<BackgroundMention> backgrounds) {
        StringBuilder sb = new StringBuilder();
        sb.append("[등장 캐릭터 매칭 (프론트 결정)]\n");
        if (mentions != null) {
            for (CharacterMention m : mentions) {
                sb.append("- @").append(m.name())
                        .append(" → modelId=").append(m.modelId())
                        .append(" (triggerWord: ").append(m.triggerWord()).append(")\n");
            }
        }
        sb.append("\n[등장 배경 매칭 (프론트 결정)]\n");
        if (backgrounds != null) {
            for (BackgroundMention b : backgrounds) {
                sb.append("- #").append(b.name())
                        .append(" → assetId=").append(b.assetId()).append("\n");
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
                log.warn("parsePanels: 'panels' key missing or not array. snippet={}",
                        snippet(response));
                return List.of();
            }
            return objectMapper.convertValue(panelsNode, new TypeReference<List<ScenarioPanel>>() {
            });
        } catch (IOException e) {
            log.warn("parsePanels: failed to parse JSON. error={}, snippet={}",
                    e.getMessage(), snippet(response));
            return List.of();
        }
    }

    private static String snippet(String s) {
        if (s == null) return "(null)";
        int max = 300;
        return s.length() <= max
                ? s
                : s.substring(0, max) + "...(" + (s.length() - max) + " chars truncated)";
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

    private Set<Long> collectValidAssetIds(List<BackgroundMention> backgrounds) {
        Set<Long> set = new HashSet<>();
        if (backgrounds != null) {
            for (BackgroundMention b : backgrounds) {
                if (b.assetId() != null) {
                    set.add(b.assetId());
                }
            }
        }
        return set;
    }

    private List<ScenarioPanel> sanitize(List<ScenarioPanel> panels,
                                         Set<Long> validModelIds,
                                         Set<Long> validAssetIds) {
        List<ScenarioPanel> result = new ArrayList<>(panels.size());
        for (ScenarioPanel p : panels) {
            Long modelId = p.characterModelId();
            if (modelId != null && !validModelIds.contains(modelId)) {
                modelId = null;
            }
            Long assetId = p.backgroundAssetId();
            if (assetId != null && !validAssetIds.contains(assetId)) {
                assetId = null;
            }
            result.add(new ScenarioPanel(
                    p.panelOrder(),
                    p.panelScenario(),
                    modelId,
                    assetId,
                    p.actionTags(),
                    p.emotionTags(),
                    p.poseTags(),
                    p.backgroundTags(),
                    p.cameraTags()));
        }
        return result;
    }
}
