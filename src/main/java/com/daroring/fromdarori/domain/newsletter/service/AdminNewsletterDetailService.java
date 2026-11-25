package com.daroring.fromdarori.domain.newsletter.service;

import com.daroring.fromdarori.domain.newsletter.dto.AdminNewsletterDetailResponse;
import com.daroring.fromdarori.domain.newsletter.dto.AdminNewsletterDetailResponse.Story;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterRepository;
import com.daroring.fromdarori.domain.story.dto.PublishedStoryContent;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AdminNewsletterDetailService {

    private final NewsletterRepository repository;
    private final ObjectMapper mapper;

    @Transactional(readOnly = true)
    public AdminNewsletterDetailResponse getDetail(UUID id) {
        var newsletter = repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NEWSLETTER4041", "뉴스레터를 찾을 수 없습니다."));
        JsonNode snapshot = mapper.readTree(newsletter.getContentSnapshot());
        List<Story> stories = PublishedStoryContent.inDisplayOrder(snapshot.path("sections"))
                .flatMap(section -> PublishedStoryContent.inDisplayOrder(section.path("stories"))
                        .map(story -> toStory(story, section.path("title").asString(null))))
                .toList();
        JsonNode quiz = mapper.readTree(newsletter.getQuizContent());
        return new AdminNewsletterDetailResponse(newsletter.getId(), newsletter.getTitle(), newsletter.getSummary(),
                newsletter.getStatus(), snapshot.path("intro").asString(null), stories,
                snapshot.path("closing").asString(null), quiz.isObject() && !quiz.isEmpty() ? quiz : null,
                newsletter.getCreatedAt(), newsletter.getScheduledAt(), newsletter.getPublishedAt());
    }

    private static Story toStory(JsonNode snapshot, String sectionTitle) {
        String id = snapshot.path("storyId").asString(null);
        return new Story(id == null ? null : UUID.fromString(id), snapshot.path("displayOrder").asInt(0),
                snapshot.path("categoryName").asString(sectionTitle), snapshot.path("title").asString(""),
                snapshot.path("body").asString(""), snapshot.path("actionUrl").asString(null),
                snapshot.path("applicationDeadline").asString(null),
                PublishedStoryContent.inDisplayOrder(snapshot.path("images")).toList());
    }
}
