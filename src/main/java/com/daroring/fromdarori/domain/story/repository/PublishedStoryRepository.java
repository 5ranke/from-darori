package com.daroring.fromdarori.domain.story.repository;

import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStory;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface PublishedStoryRepository extends Repository<NewsletterStory, UUID> {

    @Query(value = """
            select ns.id as id,
                   cast(extract(month from timezone(:issueZone, n.published_at)) as integer) as "issueMonth",
                   ns.story_snapshot ->> 'title' as title,
                   ns.story_snapshot ->> 'excerpt' as excerpt
            from newsletter_story ns
            join newsletter n on n.id = ns.newsletter_id
            where ns.category_id = :categoryId
              and n.status = 'PUBLISHED'
              and n.published_at >= :start and n.published_at < :end
            order by n.published_at desc, n.id desc, ns.display_order asc
            limit :limit offset :offset
            """, nativeQuery = true)
    List<PublishedStoryCard> findPublishedInRange(
            UUID categoryId, String issueZone, OffsetDateTime start, OffsetDateTime end, int limit, long offset);

    @Query("""
            select ns from NewsletterStory ns
            join fetch ns.newsletter n
            left join fetch ns.category
            where ns.id = :id and n.status = :status and n.publishedAt is not null
            """)
    Optional<NewsletterStory> findPublishedById(UUID id, NewsletterStatus status);

    interface PublishedStoryCard {
        UUID getId();
        int getIssueMonth();
        String getTitle();
        String getExcerpt();
    }
}
