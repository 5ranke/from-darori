package com.daroring.fromdarori.domain.admin.controller;

import com.daroring.fromdarori.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = AdminUserController.class)
public class AdminUserExceptionHandler {
    @ExceptionHandler({HandlerMethodValidationException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> invalidPagination(Exception exception) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(
                "COMMON400", "page는 0 이상의 정수, size는 1 이상의 정수여야 합니다."));
    }
}
