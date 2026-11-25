package com.daroring.fromdarori.domain.newsletter.repository;

import com.daroring.fromdarori.domain.newsletter.entity.Newsletter;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NewsletterRepository extends JpaRepository<Newsletter, UUID> {

    @Query(value = """
            select n.id as newsletterId, n.title as title, n.status as status, n.createdAt as createdAt,
                   case when n.status = :published and n.publishedAt is not null then
                       (select count(previous.id) from Newsletter previous
                        where previous.status = :published
                          and (previous.publishedAt < n.publishedAt
                               or (previous.publishedAt = n.publishedAt and previous.id <= n.id)))
                   else null end as issueNumber
            from Newsletter n
            where (:status is null or n.status = :status)
            order by n.createdAt desc, n.id desc
            """, countQuery = "select count(n) from Newsletter n where (:status is null or n.status = :status)")
    Page<AdminNewsletterRow> findAdminNewsletterPage(NewsletterStatus status, NewsletterStatus published, Pageable pageable);

    interface AdminNewsletterRow {
        UUID getNewsletterId();
        Long getIssueNumber();
        String getTitle();
        NewsletterStatus getStatus();
        OffsetDateTime getCreatedAt();
    }

    List<Newsletter> findTop3ByStatusAndPublishedAtIsNotNullOrderByPublishedAtDescIdDesc(NewsletterStatus status);

    Optional<Newsletter> findFirstByStatusAndPublishedAtIsNotNullOrderByPublishedAtDescIdDesc(NewsletterStatus status);

    Optional<Newsletter> findByIdAndStatusAndPublishedAtIsNotNull(UUID id, NewsletterStatus status);

    @Query(value = """
            select n.*
            from newsletter n
            where n.status = 'PUBLISHED' and n.published_at >= :start and n.published_at < :end
            order by n.published_at desc, n.id desc
            limit :limit offset :offset
            """, nativeQuery = true)
    List<Newsletter> findPublishedInRange(OffsetDateTime start, OffsetDateTime end, int limit, long offset);

    @Query(value = """
            select distinct cast(extract(year from timezone(:issueZone, published_at)) as integer) as issue_year
            from newsletter
            where status = 'PUBLISHED' and published_at is not null
            order by issue_year desc
            """, nativeQuery = true)
    List<Integer> findPublishedIssueYears(String issueZone);
}
