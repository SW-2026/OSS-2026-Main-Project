package com.wit.global.response;

public record ApiResponse<T>(String status, String message, T data) {
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>("201", "Created Success", data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("200", "Success", data);
    }
}