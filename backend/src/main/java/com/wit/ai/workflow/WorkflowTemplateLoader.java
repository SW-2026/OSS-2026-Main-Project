package com.wit.ai.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkflowTemplateLoader {

    private static final String TEMPLATE_BASE_PATH = "comfyui/workflows/";

    private final ObjectMapper objectMapper;

    public String load(String templateName, WorkflowParams params) {
        try {
            ClassPathResource resource = new ClassPathResource(TEMPLATE_BASE_PATH + templateName);
            if (!resource.exists()) {
                throw new IllegalStateException("Workflow template not found: " + templateName);
            }
            JsonNode root;
            try (var input = resource.getInputStream()) {
                root = objectMapper.readTree(input);
            }
            if (!root.isObject()) {
                throw new IllegalStateException("Workflow root is not an object: " + templateName);
            }
            ObjectNode workflow = (ObjectNode) root;

            // 1. KSampler 찾기 + seed 주입
            ObjectNode kSampler = findNodeByClassType(workflow, "KSampler")
                    .orElseThrow(() -> new IllegalStateException("KSampler node not found"));
            ((ObjectNode) kSampler.get("inputs")).put("seed", params.seed());

            // 2. positive 노드 ID 추출 → prompt 주입 (string 직접 또는 StringConcatenate.string_b)
            String positiveNodeId = extractConnectedNodeId(kSampler, "positive");
            injectPositivePrompt(workflow, positiveNodeId, params.positivePrompt());

            // 3. negative 노드 ID 추출 → text 직접 수정
            String negativeNodeId = extractConnectedNodeId(kSampler, "negative");
            injectTextDirectly(workflow, negativeNodeId, params.negativePrompt());

            // 4. LoRA 처리 (loraName null이거나 LoraTagLoader 없으면 skip)
            if (params.loraName() != null) {
                findNodeByClassType(workflow, "LoraTagLoader")
                        .ifPresent(node -> ((ObjectNode) node.get("inputs"))
                                .put("text", params.loraName()));
            }

            return objectMapper.writeValueAsString(workflow);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to load workflow template: " + templateName, e);
        }
    }

    private Optional<ObjectNode> findNodeByClassType(ObjectNode workflow, String classType) {
        Iterator<Map.Entry<String, JsonNode>> fields = workflow.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode node = entry.getValue();
            if (node.has("class_type") && classType.equals(node.get("class_type").asText())) {
                return Optional.of((ObjectNode) node);
            }
        }
        return Optional.empty();
    }

    private String extractConnectedNodeId(ObjectNode node, String inputName) {
        JsonNode link = node.get("inputs").get(inputName);
        if (link == null || !link.isArray() || link.isEmpty()) {
            throw new IllegalStateException("Missing input connection: " + inputName);
        }
        return link.get(0).asText();
    }

    private void injectPositivePrompt(ObjectNode workflow, String nodeId, String prompt) {
        ObjectNode targetNode = (ObjectNode) workflow.get(nodeId);
        if (targetNode == null) {
            throw new IllegalStateException("Positive node not found: " + nodeId);
        }
        ObjectNode inputs = (ObjectNode) targetNode.get("inputs");
        JsonNode textField = inputs.get("text");
        if (textField == null) {
            throw new IllegalStateException("Positive node has no 'text' input: " + nodeId);
        }
        if (textField.isArray()) {
            // 연결된 노드 추적 → StringConcatenate.string_b 수정
            String linkedNodeId = textField.get(0).asText();
            ObjectNode linkedNode = (ObjectNode) workflow.get(linkedNodeId);
            if (linkedNode == null
                    || !"StringConcatenate".equals(linkedNode.get("class_type").asText())) {
                throw new IllegalStateException(
                        "Unsupported positive prompt node chain (expected StringConcatenate): "
                                + linkedNodeId);
            }
            ((ObjectNode) linkedNode.get("inputs")).put("string_b", prompt);
        } else {
            inputs.put("text", prompt);
        }
    }

    private void injectTextDirectly(ObjectNode workflow, String nodeId, String text) {
        ObjectNode node = (ObjectNode) workflow.get(nodeId);
        if (node == null) {
            throw new IllegalStateException("Node not found: " + nodeId);
        }
        ((ObjectNode) node.get("inputs")).put("text", text);
    }
}
