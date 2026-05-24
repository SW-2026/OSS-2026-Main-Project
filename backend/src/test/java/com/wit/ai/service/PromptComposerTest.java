package com.wit.ai.service;

import com.wit.ai.dto.CharacterMention;
import com.wit.ai.dto.ComposedPrompt;
import com.wit.ai.dto.ScenarioPanel;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class PromptComposerTest {

    private ScenarioPanel samplePanel() {
        return new ScenarioPanel(
                1, "컷 1 요약", 1L,
                "running through alley, looking forward",
                "scared, urgent",
                "dynamic running, leaning forward",
                "narrow alley, neon lights",
                "wide angle, low angle"
        );
    }

    @Test
    void compose_with_mention_includes_loraName_and_joined_positive() {
        PromptComposer composer = new PromptComposer(new Random(42L));
        CharacterMention mention = new CharacterMention("연우", 1L, "yeonwoo_v1");

        ComposedPrompt result = composer.compose(samplePanel(), mention);

        assertThat(result.loraName()).isEqualTo("yeonwoo_v1");
        assertThat(result.positivePrompt())
                .isEqualTo("running through alley, looking forward, "
                        + "scared, urgent, "
                        + "dynamic running, leaning forward, "
                        + "wide angle, low angle, "
                        + "narrow alley, neon lights");
    }

    @Test
    void compose_with_null_mention_returns_null_loraName() {
        PromptComposer composer = new PromptComposer(new Random(42L));

        ComposedPrompt result = composer.compose(samplePanel(), null);

        assertThat(result.loraName()).isNull();
        assertThat(result.positivePrompt()).contains("running through alley");
    }

    @Test
    void compose_with_loraModelPath_returns_lora_tag_with_trigger() {
        PromptComposer composer = new PromptComposer(new Random(42L));
        CharacterMention mention = new CharacterMention(
                "연우", 1L, "yeonwoo_v1", "yeonwoo_v1");

        ComposedPrompt result = composer.compose(samplePanel(), mention);

        assertThat(result.loraName()).isEqualTo("<lora:yeonwoo_v1:1.0> yeonwoo_v1");
    }

    @Test
    void compose_uses_default_negative_prompt() {
        PromptComposer composer = new PromptComposer(new Random(42L));

        ComposedPrompt result = composer.compose(samplePanel(), null);

        assertThat(result.negativePrompt())
                .contains("worst quality")
                .contains("bad anatomy");
    }

    @Test
    void compose_uses_injected_random_for_seed() {
        long fixedSeed = 12345L;
        PromptComposer composer = new PromptComposer(new Random(fixedSeed));
        long expectedSeed = new Random(fixedSeed).nextLong();

        ComposedPrompt result = composer.compose(samplePanel(), null);

        assertThat(result.seed()).isEqualTo(expectedSeed);
    }

    @Test
    void compose_skips_blank_tags() {
        ScenarioPanel sparse = new ScenarioPanel(
                1, "minimal", null,
                "running", "", null, "  ", "wide angle"
        );
        PromptComposer composer = new PromptComposer(new Random(42L));

        ComposedPrompt result = composer.compose(sparse, null);

        assertThat(result.positivePrompt()).isEqualTo("running, wide angle");
    }
}
