package com.daroring.fromdarori.domain.story.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record StoryImageUpdateRequest(
        UUID imageId,
        String objectKey,
        @NotNull @Positive Integer displayOrder
) {
    @AssertTrue(message = "기존 사진 ID와 새 사진 키 중 하나만 전달해 주세요.")
    public boolean isImageReferenceValid() {
        return imageId == null ? objectKey != null && !objectKey.isBlank() : objectKey == null;
    }
}
