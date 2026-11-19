package com.daroring.fromdarori.domain.admin.dto;

import com.daroring.fromdarori.domain.admin.entity.AdminRole;
import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;

public record AdminUserCreateRequest(
        @NotBlank @Size(max = 50) String displayName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s])\\S+$") String password,
        @NotNull AdminRole role) {

    @AssertTrue(message = "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public String toString() { return "AdminUserCreateRequest[REDACTED]"; }
}
