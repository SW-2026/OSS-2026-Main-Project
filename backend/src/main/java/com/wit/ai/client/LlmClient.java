package com.wit.ai.client;

public interface LlmClient {

    String complete(String systemPrompt, String userMessage);
}
