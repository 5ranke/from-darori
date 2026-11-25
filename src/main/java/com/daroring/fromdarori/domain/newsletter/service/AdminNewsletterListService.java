package com.daroring.fromdarori.domain.newsletter.service;

import com.daroring.fromdarori.domain.newsletter.dto.AdminNewsletterListResponse;
import com.daroring.fromdarori.domain.newsletter.dto.AdminNewsletterListResponse.Item;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import com.daroring.fromdarori.domain.newsletter.repository.NewsletterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminNewsletterListService {

    private final NewsletterRepository repository;

    @Transactional(readOnly = true)
    public AdminNewsletterListResponse list(String status, String page, String size) {
        NewsletterStatus filter;
        PageRequest paging;
        try {
            filter = status == null ? null : NewsletterStatus.valueOf(status);
            paging = PageRequest.of(page == null ? 0 : Integer.parseInt(page), size == null ? 20 : Integer.parseInt(size));
            if (paging.getOffset() > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Page offset exceeds the supported range");
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "뉴스레터 목록 조회 조건을 확인해 주세요.");
        }

        var results = repository.findAdminNewsletterPage(filter, NewsletterStatus.PUBLISHED, paging);
        var items = results.getContent().stream()
                .map(row -> new Item(row.getNewsletterId(), row.getIssueNumber(), row.getTitle(), row.getStatus(), row.getCreatedAt()))
                .toList();
        return new AdminNewsletterListResponse(results.getTotalElements(), items, results.getNumber(),
                results.getSize(), results.getTotalPages(), results.hasNext());
    }
}
