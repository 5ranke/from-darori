package com.daroring.fromdarori.domain.residentreport.dto;

import com.daroring.fromdarori.domain.residentreport.entity.ResidentReport;
import com.daroring.fromdarori.domain.residentreport.entity.ResidentReportStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportIdInfoResponse(
        UUID reportId,
        String title,
        String body,
        String reporterName,
        String reporterEmail,
        ResidentReportStatus status,
        UUID approvedBy,
        OffsetDateTime approvedAt,
        OffsetDateTime createdAt
) {
    public static ReportIdInfoResponse from(ResidentReport residentReport) {
        return new ReportIdInfoResponse(
                residentReport.getId(),
                residentReport.getTitle(),
                residentReport.getBody(),
                residentReport.getReporterName(),
                residentReport.getReporterEmail(),
                residentReport.getStatus(),
                residentReport.getApprovedBy().getId(),
                residentReport.getApprovedAt(),
                residentReport.getCreatedAt()
        );
    }
}
