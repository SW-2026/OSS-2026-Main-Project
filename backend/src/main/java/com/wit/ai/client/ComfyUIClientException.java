package com.wit.ai.client;

public class ComfyUIClientException extends RuntimeException {

    public ComfyUIClientException(String message) {
        super(message);
    }

    public ComfyUIClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
