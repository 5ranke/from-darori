package com.daroring.fromdarori.domain.admin.dto;

import com.daroring.fromdarori.domain.story.entity.StorySourceType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminDashboardResponse(Statistics statistics, List<RecentStory> recentStories) {
    public record Statistics(long storyCount, long subscriberCount,
                             long publishedNewsletterCount, long pendingResidentReportCount) {}

    public record RecentStory(UUID storyId, String title, UUID categoryId, String categoryName,
                              StorySourceType sourceType, OffsetDateTime createdAt) {}
}
