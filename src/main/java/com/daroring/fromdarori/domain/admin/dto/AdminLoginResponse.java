package com.daroring.fromdarori.domain.admin.dto;

import com.daroring.fromdarori.domain.admin.entity.AdminRole;
import com.daroring.fromdarori.domain.admin.entity.AdminUser;
import java.util.UUID;

public record AdminLoginResponse(
        String accessToken, String tokenType, long expiresIn, AdminUserInfo adminUser) {

    public record AdminUserInfo(UUID id, String email, String displayName, AdminRole role) {
        public static AdminUserInfo from(AdminUser user) {
            return new AdminUserInfo(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole());
        }
    }

    @Override
    public String toString() {
        return "AdminLoginResponse[REDACTED]";
    }
}
