package com.daroring.fromdarori.domain.residentreport.dto;

import com.daroring.fromdarori.domain.residentreport.entity.ResidentReport;
import com.daroring.fromdarori.domain.residentreport.entity.ResidentReportStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ResidentReportResponse(
    UUID reportId,
    ResidentReportStatus status,
    OffsetDateTime createdAt
) {
    public static ResidentReportResponse from(ResidentReport residentReport) {
        return new ResidentReportResponse(
                residentReport.getId(),
                residentReport.getStatus(),
                residentReport.getCreatedAt()
        );
    }
}
