package com.daroring.fromdarori.domain.residentreport.repository;

import com.daroring.fromdarori.domain.residentreport.entity.ResidentReport;
import com.daroring.fromdarori.domain.residentreport.entity.ResidentReportStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

import java.util.UUID;

public interface AdminResidentReportRepository extends JpaRepository<ResidentReport, UUID> {

    Page<ResidentReport> findByStatus(ResidentReportStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select report from ResidentReport report where report.id = :id")
    Optional<ResidentReport> findByIdForUpdate(UUID id);
}
