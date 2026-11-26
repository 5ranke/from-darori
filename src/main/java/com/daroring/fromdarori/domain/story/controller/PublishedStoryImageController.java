package com.daroring.fromdarori.domain.story.controller;

import static com.daroring.fromdarori.domain.story.dto.PublishedStoryContent.IMAGE_PATH;

import com.daroring.fromdarori.domain.newsletter.repository.NewsletterStoryRepository;
import com.daroring.fromdarori.domain.story.service.StoryImageReader;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublishedStoryImageController {

    private final NewsletterStoryRepository newsletterStories;
    private final StoryImageReader images;

    @GetMapping(IMAGE_PATH + "/{newsletterId}/{objectKey}")
    public ResponseEntity<Resource> read(@PathVariable UUID newsletterId, @PathVariable String objectKey) {
        if (!newsletterStories.containsPublishedImage(newsletterId, objectKey)) {
            return ResponseEntity.notFound().build();
        }
        return images.readImage(objectKey)
                .map(image -> ResponseEntity.ok()
                        .header("X-Content-Type-Options", "nosniff")
                        .contentType(MediaType.parseMediaType(image.contentType()))
                        .body(image.resource()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
