package com.daroring.fromdarori.domain.newsletter.service;

import com.daroring.fromdarori.domain.newsletter.dto.NewsletterDetailResponse;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterDetailResponse.Quiz;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterDetailResponse.Section;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterListResponse;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterListResponse.Item;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterYearsResponse;
import com.daroring.fromdarori.domain.newsletter.entity.Newsletter;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterRepository;
import com.daroring.fromdarori.domain.story.dto.PublishedStoryContent;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;
    private final ObjectMapper objectMapper;

    public NewsletterYearsResponse getPublishedYears() {
        return new NewsletterYearsResponse(newsletterRepository.findPublishedIssueYears(Newsletter.ISSUE_ZONE.getId()));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public NewsletterListResponse getPublishedList(Integer year, int page, int size) {
        var latest = findLatestPublished();
        if (latest.isEmpty()) {
            return new NewsletterListResponse(year, page, size, false, List.of());
        }

        Newsletter latestNewsletter = latest.get();
        int selectedYear = year == null
                ? latestNewsletter.getPublishedAt().atZoneSameInstant(Newsletter.ISSUE_ZONE).getYear()
                : year;
        var start = Year.of(selectedYear).atDay(1);
        var newsletters = newsletterRepository.findPublishedInRange(
                start.atStartOfDay(Newsletter.ISSUE_ZONE).toOffsetDateTime(),
                start.plusYears(1).atStartOfDay(Newsletter.ISSUE_ZONE).toOffsetDateTime(),
                size + 1, (long) page * size);
        List<Item> items = newsletters.stream()
                .limit(size)
                .map(newsletter -> {
                    var issue = newsletter.getPublishedAt().atZoneSameInstant(Newsletter.ISSUE_ZONE);
                    return new Item(newsletter.getId(), issue.getYear(), issue.getMonthValue(),
                            newsletter.getTitle(), newsletter.getSummary(),
                            latestNewsletter.getId().equals(newsletter.getId()));
                })
                .toList();

        return new NewsletterListResponse(selectedYear, page, size, newsletters.size() > size, items);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public NewsletterDetailResponse getPublishedDetail(UUID newsletterId) {
        Newsletter newsletter = newsletterRepository
                .findByIdAndStatusAndPublishedAtIsNotNull(newsletterId, NewsletterStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "발행호를 찾을 수 없습니다."));
        var issue = newsletter.getPublishedAt().atZoneSameInstant(Newsletter.ISSUE_ZONE);
        boolean isLatest = findLatestPublished().map(Newsletter::getId).filter(newsletterId::equals).isPresent();
        List<Section> sections = PublishedStoryContent.inDisplayOrder(objectMapper.readTree(newsletter.getContentSnapshot()).path("sections"))
                .map(section -> new Section(section.path("title").asString(""),
                        PublishedStoryContent.inDisplayOrder(section.path("stories")).map(PublishedStoryContent::fromSnapshot).toList()))
                .filter(section -> !section.stories().isEmpty())
                .toList();
        return new NewsletterDetailResponse(newsletterId, issue.getYear(), issue.getMonthValue(),
                newsletter.getTitle(), newsletter.getSummary(), isLatest, sections,
                toPublicQuiz(objectMapper.readTree(newsletter.getQuizContent())));
    }

    private Optional<Newsletter> findLatestPublished() {
        return newsletterRepository
                .findFirstByStatusAndPublishedAtIsNotNullOrderByPublishedAtDescIdDesc(NewsletterStatus.PUBLISHED);
    }

    private Quiz toPublicQuiz(JsonNode snapshot) {
        String question = snapshot.path("question").asString(null);
        JsonNode choices = snapshot.path("choices");
        if (question == null || !choices.isArray()) {
            return null;
        }
        return new Quiz(question, choices.valueStream().filter(JsonNode::isString).map(JsonNode::asString).toList());
    }
}
