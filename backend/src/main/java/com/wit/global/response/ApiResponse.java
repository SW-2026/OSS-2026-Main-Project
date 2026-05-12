package com.wit.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "요청이 성공적으로 처리되었습니다.");
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, data, "요청이 성공적으로 처리되었습니다.");
    }

    /**
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
    */

    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(false, null, "[" + code + "] " + message);
    }
}