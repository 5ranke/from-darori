package com.daroring.fromdarori.domain.story.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record StoryCreateRequest(
        @NotNull(message = "소식 카테고리는 필수입니다.")
        UUID categoryId,

        @NotNull(message = "제목은 필수입니다.")
        @Size(max = 80, message = "제목은 최대 80자까지 작성할 수 있습니다.")
        String title,

        @NotNull(message = "본문은 필수입니다.")
        @Size(max = 3000, message = "본문은 최대 3,000자까지 작성할 수 있습니다.")
        String body,

        String actionUrl,
        LocalDate applicationDeadline,
        List<@Valid StoryImageRequest> images
) {
}
