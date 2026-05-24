package com.wit.ai.service;

import com.wit.ai.dto.CharacterMention;
import com.wit.ai.dto.ComposedPrompt;
import com.wit.ai.dto.ScenarioPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PromptComposer {

    private static final String DEFAULT_NEGATIVE_PROMPT =
            "worst quality, low quality, bad anatomy, blurry, watermark, signature";

    private final Random random;

    @Autowired
    public PromptComposer() {
        this(new Random());
    }

    public PromptComposer(Random random) {
        this.random = random;
    }

    public ComposedPrompt compose(ScenarioPanel panel, CharacterMention mention) {
        String positive = buildPositive(panel);
        String loraName = buildLoraName(mention);
        long seed = random.nextLong() & Long.MAX_VALUE;
        return new ComposedPrompt(positive, DEFAULT_NEGATIVE_PROMPT, seed, loraName);
    }

    private String buildLoraName(CharacterMention mention) {
        if (mention == null) return null;
        String path = mention.loraModelPath();
        String trigger = mention.triggerWord();
        if (path == null || path.isBlank()) {
            return trigger;
        }
        return (trigger == null || trigger.isBlank())
                ? "<lora:" + path + ":1.0>"
                : "<lora:" + path + ":1.0> " + trigger;
    }

    private String buildPositive(ScenarioPanel panel) {
        List<String> parts = new ArrayList<>(5);
        addIfPresent(parts, panel.actionTags());
        addIfPresent(parts, panel.emotionTags());
        addIfPresent(parts, panel.poseTags());
        addIfPresent(parts, panel.cameraTags());
        addIfPresent(parts, panel.backgroundTags());
        return String.join(", ", parts);
    }

    private void addIfPresent(List<String> parts, String tag) {
        if (tag != null && !tag.isBlank()) {
            parts.add(tag.trim());
        }
    }
}
