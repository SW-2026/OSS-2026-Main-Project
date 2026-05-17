package com.wit.ai.client;

import java.util.Optional;

public interface ComfyUIClient {

    String submitWorkflow(String workflowJson);

    Optional<ComfyUIResult> pollResult(String promptId);

    byte[] downloadImage(String filename, String subfolder);
}
