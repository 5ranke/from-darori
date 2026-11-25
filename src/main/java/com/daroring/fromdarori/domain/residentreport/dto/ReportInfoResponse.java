package com.daroring.fromdarori.domain.residentreport.dto;

import java.util.List;

public record ReportInfoResponse(
        List<ReportItemResponse> reports,
        Integer page,
        Integer size,
        Integer totalElements,
        Integer totalPages,
        Boolean hasNext
) {
}
