package com.daroring.fromdarori.domain.story.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import tools.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record PublishedStoryContent(
        String title, String body, String actionUrl, String applicationDeadline, List<Image> images
) {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Image(String url, String altText) {
    }

    public static PublishedStoryContent fromSnapshot(JsonNode story) {
        List<Image> images = inDisplayOrder(story.path("images"))
                .map(image -> new Image(image.path("url").asString(image.path("imageUrl").asString(null)),
                        image.path("altText").asString(null)))
                .filter(image -> image.url() != null)
                .toList();
        return new PublishedStoryContent(story.path("title").asString(""), story.path("body").asString(""),
                story.path("actionUrl").asString(null), story.path("applicationDeadline").asString(null), images);
    }

    public static Stream<JsonNode> inDisplayOrder(JsonNode array) {
        return array.isArray() ? array.valueStream()
                .sorted(Comparator.comparingInt(node -> node.path("displayOrder").asInt(Integer.MAX_VALUE)))
                : Stream.empty();
    }
}
