package com.daroring.fromdarori.domain.newsletter.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record NewsletterSaveRequest(
        @NotBlank @Size(max = 80) String title,
        @Size(max = 300) String summary,
        @NotBlank String intro,
        @NotEmpty List<@NotNull @Valid IncludedStory> stories,
        @NotBlank String closing,
        @NotNull @Valid Quiz quiz
) {
    public record IncludedStory(
            @NotNull UUID storyId,
            @NotNull @Positive Integer displayOrder,
            UUID categoryId,
            @NotBlank String title,
            @NotBlank String body,
            @NotBlank String categoryName,
            String actionUrl,
            LocalDate applicationDeadline,
            @NotNull List<@NotNull @Valid Image> images
    ) {
    }

    public record Image(@NotBlank String objectKey, @NotNull @Positive Integer displayOrder) {
    }

    public record Quiz(
            @NotBlank String question,
            @NotNull @Size(min = 2, max = 2) List<@NotBlank String> options,
            @NotNull @Min(0) @Max(1) Integer answer,
            @NotBlank String explanation,
            @NotNull UUID storyId,
            @NotBlank String evidence
    ) {
    }
}
