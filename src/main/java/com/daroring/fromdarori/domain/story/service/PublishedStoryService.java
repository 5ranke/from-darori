package com.daroring.fromdarori.domain.story.service;

import com.daroring.fromdarori.domain.category.entity.Category;
import com.daroring.fromdarori.domain.category.repository.CategoryRepository;
import com.daroring.fromdarori.domain.newsletter.dto.PublishedListQuery;
import com.daroring.fromdarori.domain.newsletter.entity.Newsletter;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterRepository;
import com.daroring.fromdarori.domain.story.dto.PublishedStoryContent;
import com.daroring.fromdarori.domain.story.dto.PublishedStoryDetailResponse;
import com.daroring.fromdarori.domain.story.dto.PublishedStoryListResponse;
import com.daroring.fromdarori.domain.story.dto.PublishedStoryListResponse.Item;
import com.daroring.fromdarori.domain.story.repository.PublishedStoryRepository;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class PublishedStoryService {

    private final CategoryRepository categoryRepository;
    private final NewsletterRepository newsletterRepository;
    private final PublishedStoryRepository publishedStoryRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PublishedStoryListResponse getPublishedList(UUID categoryId, PublishedListQuery query) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효한 카테고리 ID를 입력해 주세요."));
        Integer selectedYear = query.year() != null ? query.year() : latestIssueYear();
        int page = query.page();
        int size = query.size();
        if (selectedYear == null) {
            return new PublishedStoryListResponse(null, page, size, false, List.of());
        }

        var start = Year.of(selectedYear).atDay(1);
        var stories = publishedStoryRepository.findPublishedInRange(categoryId, Newsletter.ISSUE_ZONE.getId(),
                start.atStartOfDay(Newsletter.ISSUE_ZONE).toOffsetDateTime(),
                start.plusYears(1).atStartOfDay(Newsletter.ISSUE_ZONE).toOffsetDateTime(),
                size + 1, (long) page * size);
        List<Item> items = stories.stream()
                .limit(size)
                .map(story -> new Item(story.getId(), category.getName(), selectedYear, story.getIssueMonth(),
                        Objects.requireNonNullElse(story.getTitle(), ""), Objects.requireNonNullElse(story.getExcerpt(), "")))
                .toList();
        return new PublishedStoryListResponse(selectedYear, page, size, stories.size() > size, items);
    }

    @Transactional(readOnly = true)
    public PublishedStoryDetailResponse getPublishedDetail(UUID id) {
        var story = publishedStoryRepository.findPublishedById(id, NewsletterStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "수록 소식을 찾을 수 없습니다."));
        var newsletter = story.getNewsletter();
        var issue = newsletter.getPublishedAt().atZoneSameInstant(Newsletter.ISSUE_ZONE);
        var snapshot = objectMapper.readTree(story.getStorySnapshot());
        var content = PublishedStoryContent.fromSnapshot(newsletter.getId(), snapshot);
        String categoryName = story.getCategory() == null ? snapshot.path("categoryName").asString("")
                : story.getCategory().getName();
        return new PublishedStoryDetailResponse(story.getId(), newsletter.getId(), categoryName, issue.getYear(),
                issue.getMonthValue(), content.title(), content.body(), content.actionUrl(),
                content.applicationDeadline(), content.images());
    }

    private Integer latestIssueYear() {
        return newsletterRepository
                .findFirstByStatusAndPublishedAtIsNotNullOrderByPublishedAtDescIdDesc(NewsletterStatus.PUBLISHED)
                .map(newsletter -> newsletter.getPublishedAt().atZoneSameInstant(Newsletter.ISSUE_ZONE).getYear())
                .orElse(null);
    }
}
