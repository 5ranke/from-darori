package com.daroring.fromdarori.domain.admin.dto;

import com.daroring.fromdarori.domain.admin.entity.AdminRole;
import com.daroring.fromdarori.domain.admin.entity.AdminUser;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminMeResponse(UUID id, String email, String displayName, AdminRole role,
                              OffsetDateTime createdAt) {
    public static AdminMeResponse from(AdminUser user) {
        return new AdminMeResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                user.getRole(), user.getCreatedAt());
    }
}
