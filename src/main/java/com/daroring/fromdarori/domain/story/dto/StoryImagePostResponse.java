package com.daroring.fromdarori.domain.story.dto;

public record StoryImagePostResponse (
        String objectKey,
        String imageUrl,
        String fileName,
        String contentType,
        Long fileSize
) {
}
