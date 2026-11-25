package com.daroring.fromdarori.domain.newsletter.service;

import com.daroring.fromdarori.domain.admin.repository.AdminUserRepository;
import com.daroring.fromdarori.domain.category.entity.Category;
import com.daroring.fromdarori.domain.category.repository.CategoryRepository;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterSaveRequest;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterSaveRequest.Image;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterSaveRequest.IncludedStory;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterSaveResponse;
import com.daroring.fromdarori.domain.newsletter.entity.Newsletter;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStory;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterRepository;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterStoryRepository;
import com.daroring.fromdarori.domain.story.entity.Story;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import com.daroring.fromdarori.domain.story.repository.AdminStoryRepository;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class NewsletterSaveService {

    private final NewsletterRepository newsletterRepository;
    private final NewsletterStoryRepository newsletterStoryRepository;
    private final AdminStoryRepository storyRepository;
    private final CategoryRepository categoryRepository;
    private final AdminUserRepository adminRepository;
    private final Validator validator;
    private final ObjectMapper objectMapper;

    @Transactional
    public NewsletterSaveResponse save(NewsletterSaveRequest request, UUID adminId) {
        validate(request);
        var items = request.stories().stream().sorted(Comparator.comparingInt(IncludedStory::displayOrder)).toList();
        var storyIds = items.stream().map(IncludedStory::storyId).toList();
        var originals = storyRepository.findAllByIdForUpdate(storyIds).stream()
                .collect(Collectors.toMap(Story::getId, Function.identity()));
        if (originals.size() != storyIds.size()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "STORY4041", "포함된 원본 소식을 찾을 수 없습니다.");
        }

        if (newsletterStoryRepository.existsByStory_IdInAndNewsletter_StatusIn(storyIds, NewsletterStatus.HOLDING_STORIES)) {
            throw new ApiException(HttpStatus.CONFLICT, "NEWSLETTER4095", "이미 다른 뉴스레터에 담긴 소식이 있습니다.");
        }

        var categoryIds = items.stream().map(IncludedStory::categoryId).filter(Objects::nonNull).distinct().toList();
        var orderedCategories = categoryRepository.findAllByOrderByDisplayOrderAsc();
        var categories = orderedCategories.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        if (!categories.keySet().containsAll(categoryIds)) {
            throw invalid("포함된 소식의 카테고리를 확인해 주세요.");
        }
        var author = adminRepository.findById(adminId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH4012", "운영자 인증이 필요합니다."));

        var snapshots = items.stream().map(NewsletterSaveService::snapshot).toList();
        var categoryOrder = NewsletterCategoryOrder.byCategory(orderedCategories, StorySnapshot::categoryId,
                StorySnapshot::categoryName);
        var grouped = new LinkedHashMap<String, List<StorySnapshot>>();
        snapshots.stream().sorted(categoryOrder.thenComparingInt(StorySnapshot::displayOrder))
                .forEach(snapshot -> grouped.computeIfAbsent(snapshot.categoryName(), key -> new ArrayList<>()).add(snapshot));
        var sections = grouped.entrySet().stream().map(entry -> new Section(entry.getKey(), entry.getValue())).toList();
        var content = new ContentSnapshot(request.intro(), request.closing(), sections);
        Newsletter newsletter = newsletterRepository.save(Newsletter.saveDraft(author, request.title(), request.summary(),
                objectMapper.writeValueAsString(content), objectMapper.writeValueAsString(request.quiz())));

        var included = snapshots.stream().map(snapshot -> NewsletterStory.include(newsletter, originals.get(snapshot.storyId()),
                categories.get(snapshot.categoryId()), snapshot.displayOrder(), objectMapper.writeValueAsString(snapshot))).toList();
        newsletterStoryRepository.saveAllAndFlush(included);
        return new NewsletterSaveResponse(newsletter.getId(), newsletter.getTitle(), newsletter.getStatus(),
                included.size(), adminId, newsletter.getCreatedAt());
    }

    @Transactional
    public UUID withdraw(UUID id) {
        Newsletter newsletter = newsletterRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NEWSLETTER4041", "뉴스레터를 찾을 수 없습니다."));
        if (newsletter.getStatus() != NewsletterStatus.SAVED) {
            throw new ApiException(HttpStatus.CONFLICT, "NEWSLETTER4096", "저장 상태인 뉴스레터만 저장을 취소할 수 있습니다.");
        }
        newsletterRepository.delete(newsletter);
        return id;
    }

    private void validate(NewsletterSaveRequest request) {
        if (request == null || !validator.validate(request).isEmpty()) {
            throw invalid("뉴스레터 제목, 인사말, 수록 소식, 맺음말과 퀴즈를 확인해 주세요.");
        }
        var stories = request.stories();
        if (stories.stream().map(IncludedStory::storyId).distinct().count() != stories.size()
                || stories.stream().map(IncludedStory::displayOrder).distinct().count() != stories.size()) {
            throw invalid("수록 소식과 수록 순서는 중복될 수 없습니다.");
        }
        for (IncludedStory story : stories) {
            var images = story.images();
            if (images.stream().map(Image::displayOrder).distinct().count() != images.size()) {
                throw invalid("같은 소식의 사진 표시 순서는 중복될 수 없습니다.");
            }
        }
        var quiz = request.quiz();
        if (stories.stream().noneMatch(story -> story.storyId().equals(quiz.storyId()))
                || quiz.options().get(0).strip().equals(quiz.options().get(1).strip())) {
            throw invalid("퀴즈는 수록 소식과 서로 다른 보기 두 개가 필요합니다.");
        }
    }

    private static StorySnapshot snapshot(IncludedStory story) {
        String body = story.body();
        String firstParagraph = body.strip().split("\\R\\s*\\R", 2)[0].replaceAll("\\s+", " ");
        int length = firstParagraph.codePointCount(0, firstParagraph.length());
        String excerpt = length > 160
                ? firstParagraph.substring(0, firstParagraph.offsetByCodePoints(0, 160)).stripTrailing() + "…"
                : firstParagraph;
        return new StorySnapshot(story.storyId(), story.displayOrder(), story.categoryId(), story.categoryName(),
                story.title(), body, excerpt, story.actionUrl(), story.applicationDeadline(),
                story.images().stream().sorted(Comparator.comparingInt(Image::displayOrder)).toList());
    }

    private static ApiException invalid(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "NEWSLETTER4001", message);
    }

    private record ContentSnapshot(String intro, String closing, List<Section> sections) {
    }

    private record Section(String title, List<StorySnapshot> stories) {
    }

    private record StorySnapshot(UUID storyId, int displayOrder, UUID categoryId, String categoryName, String title,
                                 String body, String excerpt, String actionUrl, LocalDate applicationDeadline,
                                 List<Image> images) {
    }
}
