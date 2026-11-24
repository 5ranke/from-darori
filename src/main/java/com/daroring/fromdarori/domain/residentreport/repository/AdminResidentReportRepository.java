package com.daroring.fromdarori.domain.residentreport.repository;

import com.daroring.fromdarori.domain.residentreport.entity.ResidentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminResidentReportRepository extends JpaRepository<ResidentReport, UUID> {
}
