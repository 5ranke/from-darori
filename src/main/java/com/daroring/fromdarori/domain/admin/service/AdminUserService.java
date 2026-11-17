package com.daroring.fromdarori.domain.admin.service;

import com.daroring.fromdarori.domain.admin.dto.AdminUserListResponse;
import com.daroring.fromdarori.domain.admin.dto.AdminUserCreateRequest;
import com.daroring.fromdarori.domain.admin.dto.AdminUserDeleteResponse;
import com.daroring.fromdarori.domain.admin.exception.AdminUserDeletionException;
import com.daroring.fromdarori.domain.admin.exception.AdminUserDeletionException.Reason;
import java.util.UUID;
import com.daroring.fromdarori.domain.admin.entity.AdminUser;
import com.daroring.fromdarori.domain.admin.exception.DuplicateAdminEmailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;
import com.daroring.fromdarori.domain.admin.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminUserListResponse.UserInfo create(AdminUserCreateRequest request) {
        if (adminUserRepository.existsByEmail(request.email())) {
            throw new DuplicateAdminEmailException();
        }
        var user = AdminUser.create(request.displayName(), request.email(),
                passwordEncoder.encode(request.password()), request.role());
        try {
            user = adminUserRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            // Also handle two concurrent requests that passed the initial duplicate check.
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "admin_user_email_key".equals(violation.getConstraintName())) {
                    throw new DuplicateAdminEmailException();
                }
            }
            throw exception;
        }
        return AdminUserListResponse.UserInfo.from(user);
    }

    @Transactional
    public AdminUserDeleteResponse delete(UUID adminUserId, UUID currentAdminId) {
        // Lock in a fixed order so concurrent deletions cannot both remove the last accounts.
        var users = adminUserRepository.findAllForDeletion();
        AdminUser target = users.stream().filter(user -> user.getId().equals(adminUserId))
                .findFirst().orElseThrow(() -> new AdminUserDeletionException(Reason.NOT_FOUND));
        if (adminUserId.equals(currentAdminId)) {
            throw new AdminUserDeletionException(Reason.SELF);
        }
        if (users.size() <= 1) {
            throw new AdminUserDeletionException(Reason.LAST);
        }
        if (users.stream().noneMatch(user -> user.getId().equals(currentAdminId))) {
            throw new AdminUserDeletionException(Reason.UNAUTHENTICATED);
        }
        try {
            adminUserRepository.delete(target);
            // Let PostgreSQL apply ON DELETE SET NULL; do not cascade-delete authored content.
            adminUserRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new AdminUserDeletionException(Reason.RELATED_DATA);
        }
        return new AdminUserDeleteResponse(adminUserId);
    }

    @Transactional(readOnly = true)
    public AdminUserListResponse list(int page, int size) {
        // Stable ordering also when multiple accounts share the same creation timestamp.
        var pageable = PageRequest.of(page, size, Sort.by("createdAt", "id"));
        return AdminUserListResponse.from(adminUserRepository.findAll(pageable));
    }
}
