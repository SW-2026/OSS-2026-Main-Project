package com.wit.ai.dto;

public record ScenarioPanel(
        int panelOrder,
        String panelScenario,
        Long characterModelId,
        Long backgroundAssetId,
        String actionTags,
        String emotionTags,
        String poseTags,
        String backgroundTags,
        String cameraTags
) {
    // 기존 8-arg 호출 호환 — backgroundAssetId 기본 null (CharacterMention 패턴)
    public ScenarioPanel(int panelOrder, String panelScenario, Long characterModelId,
                         String actionTags, String emotionTags, String poseTags,
                         String backgroundTags, String cameraTags) {
        this(panelOrder, panelScenario, characterModelId, null,
             actionTags, emotionTags, poseTags, backgroundTags, cameraTags);
    }
}
