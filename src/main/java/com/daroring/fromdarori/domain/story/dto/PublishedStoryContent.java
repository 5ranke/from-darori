package com.daroring.fromdarori.domain.story.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.web.util.UriUtils;
import tools.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record PublishedStoryContent(
        String title, String body, String actionUrl, String applicationDeadline, List<Image> images
) {
    public static final String IMAGE_PATH = "/api/v1/published-story-images";
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Image(String url, String altText) {
    }

    public static PublishedStoryContent fromSnapshot(UUID newsletterId, JsonNode story) {
        return new PublishedStoryContent(story.path("title").asString(""), story.path("body").asString(""),
                story.path("actionUrl").asString(null), story.path("applicationDeadline").asString(null),
                imagesFromSnapshot(newsletterId, story).toList());
    }

    public static Stream<Image> imagesFromSnapshot(UUID newsletterId, JsonNode story) {
        return inDisplayOrder(story.path("images"))
                .map(image -> new Image(imageUrl(newsletterId, image), image.path("altText").asString(null)))
                .filter(image -> image.url() != null && !image.url().isBlank());
    }

    private static String imageUrl(UUID newsletterId, JsonNode image) {
        String key = image.path("objectKey").asString("");
        if (!key.isBlank()) {
            return IMAGE_PATH + "/" + newsletterId + "/" + UriUtils.encodePathSegment(key, StandardCharsets.UTF_8);
        }
        return image.path("url").asString(image.path("imageUrl").asString(null));
    }

    public static Stream<JsonNode> inDisplayOrder(JsonNode array) {
        return array.isArray() ? array.valueStream()
                .sorted(Comparator.comparingInt(node -> node.path("displayOrder").asInt(Integer.MAX_VALUE)))
                : Stream.empty();
    }
}
