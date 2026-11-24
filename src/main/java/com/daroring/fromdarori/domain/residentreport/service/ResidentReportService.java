package com.daroring.fromdarori.domain.residentreport.service;

import com.daroring.fromdarori.domain.residentreport.dto.ResidentReportRequest;
import com.daroring.fromdarori.domain.residentreport.dto.ResidentReportResponse;
import com.daroring.fromdarori.domain.residentreport.entity.ResidentReport;
import com.daroring.fromdarori.domain.residentreport.repository.ResidentReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResidentReportService {
    private final ResidentReportRepository residentReportRepository;

    @Transactional
    public ResidentReportResponse create(ResidentReportRequest request) {
        ResidentReport residentReport = residentReportRepository.save(ResidentReport.create(
                request.title(), request.body(), request.reporterName(), request.reporterEmail()
        ));
        return ResidentReportResponse.from(residentReport);
    }
}
