package com.daroring.fromdarori.domain.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminLoginRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank String password,
        Boolean rememberMe) {

    public boolean isRememberMe() {
        return Boolean.TRUE.equals(rememberMe);
    }

    @Override
    public String toString() {
        return "AdminLoginRequest[REDACTED]";
    }
}
