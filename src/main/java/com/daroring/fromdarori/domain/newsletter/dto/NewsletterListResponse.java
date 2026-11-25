package com.daroring.fromdarori.domain.newsletter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record NewsletterListResponse(Integer selectedYear, int page, int size, boolean hasNext, List<Item> items) {

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Item(UUID newsletterId, int issueYear, int issueMonth, String title, String summary, boolean isLatest) {
    }
}
