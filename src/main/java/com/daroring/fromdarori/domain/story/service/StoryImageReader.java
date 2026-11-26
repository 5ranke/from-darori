package com.daroring.fromdarori.domain.story.service;

import java.util.Optional;
import org.springframework.core.io.Resource;

public interface StoryImageReader {

    Optional<Image> readImage(String objectKey);

    record Image(Resource resource, String contentType) {
    }
}
