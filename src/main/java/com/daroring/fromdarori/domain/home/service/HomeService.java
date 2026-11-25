package com.daroring.fromdarori.domain.home.service;

import com.daroring.fromdarori.domain.home.dto.HomeResponse;
import com.daroring.fromdarori.domain.home.dto.HomeResponse.FeaturedStory;
import com.daroring.fromdarori.domain.home.dto.HomeResponse.RecentNewsletter;
import com.daroring.fromdarori.domain.newsletter.entity.Newsletter;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStory;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterRepository;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterStoryRepository;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private static final ZoneId ISSUE_ZONE = ZoneId.of("Asia/Seoul");
    private static final List<String> FEATURED_CATEGORY_CODES =
            List.of("RECRUITMENT", "PROJECT_FOLLOW_UP", "SEASONAL");

    private final NewsletterRepository newsletterRepository;
    private final NewsletterStoryRepository newsletterStoryRepository;
    private final ObjectMapper objectMapper;

    public HomeResponse getHome() {
        List<Newsletter> newsletters = newsletterRepository
                .findTop3ByStatusAndPublishedAtIsNotNullOrderByPublishedAtDescIdDesc(NewsletterStatus.PUBLISHED);
        if (newsletters.isEmpty()) {
            return new HomeResponse(null, List.of(), List.of());
        }

        UUID latestNewsletterId = newsletters.get(0).getId();
        List<UUID> newsletterIds = newsletters.stream().map(Newsletter::getId).toList();
        Map<String, FeaturedStory> featuredByCategory = new HashMap<>();
        Map<UUID, String> thumbnailByNewsletter = new HashMap<>();

        for (NewsletterStory story : newsletterStoryRepository.findIncludedStories(newsletterIds)) {
            UUID newsletterId = story.getNewsletter().getId();
            JsonNode snapshot = objectMapper.readTree(story.getStorySnapshot());
            thumbnailByNewsletter.computeIfAbsent(newsletterId, id -> firstImageUrl(snapshot));

            if (latestNewsletterId.equals(newsletterId) && story.getCategory() != null) {
                String categoryCode = story.getCategory().getCode();
                if (FEATURED_CATEGORY_CODES.contains(categoryCode)) {
                    featuredByCategory.computeIfAbsent(categoryCode, code -> new FeaturedStory(
                            story.getId(), story.getCategory().getName(),
                            snapshot.path("title").asString(""), snapshot.path("excerpt").asString("")));
                }
            }
        }

        List<FeaturedStory> featuredStories = FEATURED_CATEGORY_CODES.stream()
                .map(featuredByCategory::get)
                .filter(Objects::nonNull)
                .toList();
        List<RecentNewsletter> recentNewsletters = newsletters.stream()
                .map(newsletter -> {
                    var issue = newsletter.getPublishedAt().atZoneSameInstant(ISSUE_ZONE);
                    return new RecentNewsletter(newsletter.getId(), issue.getYear(), issue.getMonthValue(),
                            newsletter.getTitle(), newsletter.getSummary(), thumbnailByNewsletter.get(newsletter.getId()));
                })
                .toList();
        return new HomeResponse(latestNewsletterId, featuredStories, recentNewsletters);
    }

    private String firstImageUrl(JsonNode snapshot) {
        return snapshot.path("images").valueStream()
                .filter(image -> image.path("imageUrl").isTextual())
                .min(Comparator.comparingInt(image -> image.path("displayOrder").asInt(Integer.MAX_VALUE)))
                .map(image -> image.path("imageUrl").asString())
                .orElse(null);
    }
}
