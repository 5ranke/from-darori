package com.daroring.fromdarori.domain.residentreport.dto;

import com.daroring.fromdarori.domain.residentreport.entity.ResidentReport;
import com.daroring.fromdarori.domain.residentreport.entity.ResidentReportStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportItemResponse(
        UUID reportId,
        String title,
        String reporterName,
        ResidentReportStatus status,
        OffsetDateTime createdAt
) {
    public static ReportItemResponse from(ResidentReport residentReport) {
        return new ReportItemResponse(
                residentReport.getId(),
                residentReport.getTitle(),
                residentReport.getReporterName(),
                residentReport.getStatus(),
                residentReport.getCreatedAt()
        );
    }
}
