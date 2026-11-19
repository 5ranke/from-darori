package com.daroring.fromdarori.domain.story.dto;

import jakarta.validation.constraints.NotNull;

public record StoryImageRequest(
        @NotNull
        String objectKey,

        // 서버가 images 배열 순서로 결정하므로 입력은 선택 사항입니다.
        Integer displayOrder
) {
}
