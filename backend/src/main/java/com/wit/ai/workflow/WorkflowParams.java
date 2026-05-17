package com.wit.ai.workflow;

public record WorkflowParams(
        String positivePrompt,
        String negativePrompt,
        long seed,
        String loraName            // nullable — null이면 LoraTagLoader 노드 안 건드림
) {}
