package com.daroring.fromdarori.domain.story.dto;

public record StoryImagePostResponse (
        // objectKey와 imageUrl은 임시로 savedFilename으로 대신함.
        String objectKey,
        String imageUrl,
        String fileName,
        String contentType,
        Long fileSize
) {
}
