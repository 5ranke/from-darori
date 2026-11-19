package com.daroring.fromdarori.domain.admin.controller;

import com.daroring.fromdarori.global.response.ApiResponse;
import com.daroring.fromdarori.domain.admin.exception.DuplicateAdminEmailException;
import com.daroring.fromdarori.domain.admin.exception.AdminUserDeletionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = AdminUserController.class)
public class AdminUserExceptionHandler {
    @ExceptionHandler(AdminUserDeletionException.class)
    public ResponseEntity<ApiResponse<Void>> deletionFailure(AdminUserDeletionException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.failure(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(DuplicateAdminEmailException.class)
    public ResponseEntity<ApiResponse<Void>> duplicateEmail(DuplicateAdminEmailException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure("USER4091", exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> invalidCreateRequest(Exception exception) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(
                "USER4001", "이름·이메일·비밀번호·역할의 입력값을 확인해 주세요."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> invalidParameter(MethodArgumentTypeMismatchException exception) {
        String message = "adminUserId".equals(exception.getName())
                ? "adminUserId는 UUID 형식이어야 합니다."
                : "page는 0 이상의 정수, size는 1 이상의 정수여야 합니다.";
        return ResponseEntity.badRequest().body(ApiResponse.failure("COMMON400", message));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> invalidPagination(Exception exception) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(
                "COMMON400", "page는 0 이상의 정수, size는 1 이상의 정수여야 합니다."));
    }
}
