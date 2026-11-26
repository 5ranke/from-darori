package com.daroring.fromdarori.domain.newsletter.repository;

import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStory;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NewsletterStoryRepository extends JpaRepository<NewsletterStory, UUID> {

    boolean existsByStory_IdInAndNewsletter_StatusIn(Collection<UUID> storyIds, Collection<NewsletterStatus> statuses);

    @Query("""
            select ns from NewsletterStory ns
            left join fetch ns.category
            where ns.newsletter.id in :newsletterIds
            order by ns.displayOrder
            """)
    List<NewsletterStory> findIncludedStories(Collection<UUID> newsletterIds);

    @Query(value = """
            select exists (
                select 1 from newsletter_story ns
                join newsletter n on n.id = ns.newsletter_id
                where n.id = :newsletterId and n.status = 'PUBLISHED' and n.published_at is not null
                  and ns.story_snapshot @> jsonb_build_object(
                    'images', jsonb_build_array(jsonb_build_object('objectKey', cast(:objectKey as text))))
            )
            """, nativeQuery = true)
    boolean containsPublishedImage(UUID newsletterId, String objectKey);
}
