package com.wit.global.error;

import com.wit.global.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * IllegalArgumentException: 잘못된 인자 전달 (400 Bad Request)
     * 예: 중복 이메일 가입 시도, 비밀번호 불일치 등
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("잘못된 요청 발생: {}", e.getMessage());
        return ApiResponse.error(400, e.getMessage());
    }

    /**
     * EntityNotFoundException: 존재하지 않는 엔티티 조회 (404 Not Found)
     * 예: 존재하지 않는 회원 ID로 조회 시
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("리소스 찾기 실패: {}", e.getMessage());
        return ApiResponse.error(404, e.getMessage());
    }

    /**
     * AccessDeniedException: 인증은 되었으나 권한이 부족한 경우 (403 Forbidden)
     * 예: 다른 회원의 리소스에 접근 시도
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("접근 권한 없음: {}", e.getMessage());
        return ApiResponse.error(403, e.getMessage());
    }

    /**
     * MethodArgumentNotValidException: @Valid 검증 실패 (400 Bad Request)
     * 필드별 에러 메시지를 data 맵에 담아 반환.
     * 예: { "modelName": "모델명은 필수입니다." }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (a, b) -> a  // 동일 필드 중복 시 첫 메시지 유지
                ));
        log.warn("입력값 검증 실패: {}", errors);
        return new ApiResponse<>(false, errors, "입력값 검증 실패");
    }

    /**
     * 기타 모든 런타임 예외 (500 Internal Server Error)
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("서버 내부 오류 발생: ", e);
        return ApiResponse.error(500, "서버 내부 오류가 발생했습니다.");
    }
}
