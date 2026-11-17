package com.daroring.fromdarori.domain.admin.service;

import com.daroring.fromdarori.domain.admin.dto.AdminUserListResponse;
import com.daroring.fromdarori.domain.admin.dto.AdminUserCreateRequest;
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

    @Transactional(readOnly = true)
    public AdminUserListResponse list(int page, int size) {
        // Stable ordering also when multiple accounts share the same creation timestamp.
        var pageable = PageRequest.of(page, size, Sort.by("createdAt", "id"));
        return AdminUserListResponse.from(adminUserRepository.findAll(pageable));
    }
}
