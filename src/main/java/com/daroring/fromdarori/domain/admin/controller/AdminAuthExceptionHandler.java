package com.daroring.fromdarori.domain.admin.controller;

import com.daroring.fromdarori.global.response.ApiResponse;
import com.daroring.fromdarori.domain.admin.exception.RefreshTokenException;
import com.daroring.fromdarori.domain.admin.exception.DeletedAdminException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AdminAuthController.class)
public class AdminAuthExceptionHandler {

    @ExceptionHandler(DeletedAdminException.class)
    public ResponseEntity<ApiResponse<Void>> deletedAdmin(DeletedAdminException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure("AUTH4016", exception.getMessage()));
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiResponse<Void>> invalidRefreshToken(RefreshTokenException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> invalidCredentials(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(
                "AUTH4011", "이메일 또는 비밀번호가 일치하지 않습니다."));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> invalidRequest(Exception exception) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(
                "COMMON400", "요청 형식과 필수 입력값을 확인해 주세요."));
    }
}
