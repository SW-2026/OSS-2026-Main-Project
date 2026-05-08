/*
package com.wit.global.test;

import com.wit.global.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    // 1. 성공 응답 테스트
    @GetMapping("/success")
    public ApiResponse<String> testSuccess() {
        return ApiResponse.ok("연결 성공!");
    }

    // 2. 400 Bad Request 테스트 (IllegalArgumentException)
    @GetMapping("/400")
    public void test400() {
        throw new IllegalArgumentException("GlobalExceptionHandler가 400으로 처리하는지 확인");
    }

    // 3. 404 Not Found 테스트 (EntityNotFoundException)
    @GetMapping("/404")
    public void test404() {
        throw new EntityNotFoundException("GlobalExceptionHandler가 404로 처리하는지 확인");
    }
}
 */
