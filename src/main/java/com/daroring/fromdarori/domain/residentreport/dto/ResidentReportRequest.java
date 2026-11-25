package com.daroring.fromdarori.domain.residentreport.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResidentReportRequest(
        @NotNull(message = "제목은 필수입니다.")
        @Size(max=80, message = "제목은 최대 80자까지 작성할 수 있습니다.")
        String title,

        @NotNull(message = "내용은 필수입니다.")
        @Size(max=3000, message = "내용은 최대 3,000자까지 작성할 수 있습니다.")
        String body,

        @Size(max=100, message = "제보자 이름은 최대 100자까지 작성할 수 있습니다.")
        String reporterName,

        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String reporterEmail
) {
}
