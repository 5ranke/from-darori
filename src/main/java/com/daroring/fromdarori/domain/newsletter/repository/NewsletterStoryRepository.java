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
}
