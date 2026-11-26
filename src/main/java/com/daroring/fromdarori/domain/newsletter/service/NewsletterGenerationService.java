package com.daroring.fromdarori.domain.newsletter.service;

import com.daroring.fromdarori.domain.newsletter.client.OpenAiNewsletterClient;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterGenerateRequest;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterGenerateResponse;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterGenerateResponse.Image;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterGenerateResponse.StoryDraft;
import com.daroring.fromdarori.domain.story.entity.Story;
import com.daroring.fromdarori.domain.story.entity.StoryImage;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import com.daroring.fromdarori.domain.story.repository.AdminStoryImageRepository;
import com.daroring.fromdarori.domain.story.repository.AdminStoryRepository;
import com.daroring.fromdarori.domain.story.service.StoryImageStorage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsletterGenerationService {

    private final AdminStoryRepository storyRepository;
    private final AdminStoryImageRepository imageRepository;
    private final StoryImageStorage imageStorage;
    private final OpenAiNewsletterClient openAi;

    public NewsletterGenerateResponse generate(NewsletterGenerateRequest request) {
        if (request == null || request.title() == null || request.title().isBlank() || request.title().length() > 80
                || request.storyIds() == null || request.storyIds().isEmpty()
                || request.storyIds().stream().anyMatch(Objects::isNull)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NEWSLETTER4001",
                    "80자 이내의 뉴스레터 제목과 소식을 한 건 이상 입력해 주세요.");
        }

        var ids = request.storyIds();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NEWSLETTER4001", "같은 소식을 중복 선택할 수 없습니다.");
        }

        var storiesById = storyRepository.findAllWithCategoryByIdIn(ids).stream()
                .collect(Collectors.toMap(Story::getId, Function.identity()));
        if (storiesById.size() != ids.size()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "STORY4041", "선택한 원본 소식 중 찾을 수 없는 소식이 있습니다.");
        }

        var imagesByStory = imageRepository.findByStory_IdInOrderByDisplayOrderAsc(ids).stream()
                .collect(Collectors.groupingBy(image -> image.getStory().getId()));
        var stories = ids.stream().map(storiesById::get).toList();
        var sources = stories.stream()
                .map(story -> new OpenAiNewsletterClient.Source(story.getId(), story.getTitle(), story.getBody(),
                        story.getCategory().getName(), Objects.toString(story.getApplicationDeadline(), null)))
                .toList();
        String title = request.title().strip();
        var draft = openAi.generate(title, sources);
        var resultStories = new ArrayList<StoryDraft>(stories.size());
        for (Story story : stories) {
            var generated = draft.stories().get(story.getId());
            var category = story.getCategory();
            List<Image> images = imagesByStory.getOrDefault(story.getId(), List.of()).stream()
                    .map(this::toDraftImage)
                    .toList();
            resultStories.add(new StoryDraft(story.getId(), resultStories.size() + 1, category.getId(),
                    generated.title(), generated.body(), category.getName(), story.getActionUrl(),
                    story.getApplicationDeadline(), images));
        }
        return new NewsletterGenerateResponse(title, draft.summary(), draft.intro(), resultStories,
                draft.closing(), draft.quiz());
    }

    private Image toDraftImage(StoryImage image) {
        return new Image(image.getObjectKey(), imageStorage.url(image.getObjectKey()), image.getDisplayOrder());
    }
}
