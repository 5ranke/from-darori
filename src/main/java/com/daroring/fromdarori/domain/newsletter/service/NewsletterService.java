package com.daroring.fromdarori.domain.newsletter.service;

import com.daroring.fromdarori.domain.newsletter.dto.NewsletterListResponse;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterListResponse.Item;
import com.daroring.fromdarori.domain.newsletter.dto.NewsletterYearsResponse;
import com.daroring.fromdarori.domain.newsletter.entity.Newsletter;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterRepository;
import java.time.Year;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;

    public NewsletterYearsResponse getPublishedYears() {
        return new NewsletterYearsResponse(newsletterRepository.findPublishedIssueYears(Newsletter.ISSUE_ZONE.getId()));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public NewsletterListResponse getPublishedList(Integer year, int page, int size) {
        var latest = newsletterRepository
                .findFirstByStatusAndPublishedAtIsNotNullOrderByPublishedAtDescIdDesc(NewsletterStatus.PUBLISHED);
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
}
