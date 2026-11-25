package com.daroring.fromdarori.domain.home.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record HomeResponse(
        UUID latestNewsletterId,
        List<FeaturedStory> featuredStories,
        List<RecentNewsletter> recentNewsletters
) {
    public record FeaturedStory(UUID publishedStoryId, String categoryName, String title, String excerpt) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record RecentNewsletter(
            UUID newsletterId, int issueYear, int issueMonth, String title, String summary, String thumbnailUrl
    ) {
    }
}
