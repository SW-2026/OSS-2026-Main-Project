package com.wit.ai.dto;

public record ScenarioPanel(
        int panelOrder,
        String panelScenario,
        Long characterModelId,
        String actionTags,
        String emotionTags,
        String poseTags,
        String backgroundTags,
        String cameraTags
) {
}
