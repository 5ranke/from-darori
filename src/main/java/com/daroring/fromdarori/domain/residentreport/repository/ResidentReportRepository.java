package com.daroring.fromdarori.domain.residentreport.repository;

import com.daroring.fromdarori.domain.residentreport.entity.ResidentReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResidentReportRepository extends JpaRepository<ResidentReport, UUID> {
}
