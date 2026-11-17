package com.daroring.fromdarori.domain.admin.dto;

import com.daroring.fromdarori.domain.admin.entity.AdminRole;
import com.daroring.fromdarori.domain.admin.entity.AdminUser;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record AdminUserListResponse(long totalCount, List<UserInfo> users, int page,
                                    int size, int totalPages, boolean hasNext) {
    public static AdminUserListResponse from(Page<AdminUser> users) {
        return new AdminUserListResponse(users.getTotalElements(),
                users.getContent().stream().map(UserInfo::from).toList(),
                users.getNumber(), users.getSize(), users.getTotalPages(), users.hasNext());
    }

    public record UserInfo(UUID id, String email, String displayName, AdminRole role,
                           OffsetDateTime createdAt) {
        public static UserInfo from(AdminUser user) {
            return new UserInfo(user.getId(), user.getEmail(), user.getDisplayName(),
                    user.getRole(), user.getCreatedAt());
        }
    }
}
