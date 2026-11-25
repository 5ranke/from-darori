package com.daroring.fromdarori.domain.newsletter.repository;

import com.daroring.fromdarori.domain.newsletter.entity.Newsletter;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NewsletterRepository extends JpaRepository<Newsletter, UUID> {

    List<Newsletter> findTop3ByStatusAndPublishedAtIsNotNullOrderByPublishedAtDescIdDesc(NewsletterStatus status);

    Optional<Newsletter> findFirstByStatusAndPublishedAtIsNotNullOrderByPublishedAtDescIdDesc(NewsletterStatus status);

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
