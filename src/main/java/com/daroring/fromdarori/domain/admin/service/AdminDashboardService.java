package com.daroring.fromdarori.domain.admin.service;

import com.daroring.fromdarori.domain.admin.dto.AdminDashboardResponse;
import com.daroring.fromdarori.domain.admin.repository.AdminDashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final AdminDashboardRepository adminDashboardRepository;

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public AdminDashboardResponse getDashboard() {
        return new AdminDashboardResponse(adminDashboardRepository.statistics(),
                adminDashboardRepository.recentStories());
    }
}
