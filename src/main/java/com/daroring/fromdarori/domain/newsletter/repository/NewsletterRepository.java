package com.daroring.fromdarori.domain.newsletter.repository;

import com.daroring.fromdarori.domain.newsletter.entity.Newsletter;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NewsletterRepository extends JpaRepository<Newsletter, UUID> {

    List<Newsletter> findTop3ByStatusAndPublishedAtIsNotNullOrderByPublishedAtDescIdDesc(NewsletterStatus status);

    @Query(value = """
            select distinct cast(extract(year from timezone(:issueZone, published_at)) as integer) as issue_year
            from newsletter
            where status = 'PUBLISHED' and published_at is not null
            order by issue_year desc
            """, nativeQuery = true)
    List<Integer> findPublishedIssueYears(String issueZone);
}
