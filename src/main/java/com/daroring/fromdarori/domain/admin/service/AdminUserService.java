package com.daroring.fromdarori.domain.admin.service;

import com.daroring.fromdarori.domain.admin.dto.AdminUserListResponse;
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

    @Transactional(readOnly = true)
    public AdminUserListResponse list(int page, int size) {
        // Stable ordering also when multiple accounts share the same creation timestamp.
        var pageable = PageRequest.of(page, size, Sort.by("createdAt", "id"));
        return AdminUserListResponse.from(adminUserRepository.findAll(pageable));
    }
}
