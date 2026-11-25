package com.daroring.fromdarori.domain.story.repository;

import com.daroring.fromdarori.domain.story.entity.Story;
import com.daroring.fromdarori.domain.story.entity.StorySourceType;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface AdminStoryRepository extends JpaRepository<Story, UUID> {

    @Query("select s from Story s join fetch s.category where s.id in :ids")
    List<Story> findAllWithCategoryByIdIn(@Param("ids") Collection<UUID> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Story s where s.id = :id")
    Optional<Story> findByIdForUpdate(@Param("id") UUID id);

    @Query(value = """
            select s from Story s
            join fetch s.category c
            left join s.residentReport rr
            where (:categoryId is null or c.id = :categoryId)
              and (:sourceType is null or s.sourceType = :sourceType)
              and (s.sourceType = com.daroring.fromdarori.domain.story.entity.StorySourceType.MANUAL
                   or rr.status = com.daroring.fromdarori.domain.residentreport.entity.ResidentReportStatus.APPROVED)
            """,
            countQuery = """
            select count(s) from Story s
            left join s.residentReport rr
            where (:categoryId is null or s.category.id = :categoryId)
              and (:sourceType is null or s.sourceType = :sourceType)
              and (s.sourceType = com.daroring.fromdarori.domain.story.entity.StorySourceType.MANUAL
                   or rr.status = com.daroring.fromdarori.domain.residentreport.entity.ResidentReportStatus.APPROVED)
            """)
    Page<Story> findAdminStories(
            @Param("categoryId") UUID categoryId,
            @Param("sourceType") StorySourceType sourceType,
            Pageable pageable
    );

    @Query(value = """
            select s from Story s
            join fetch s.category c
            left join s.residentReport rr
            where (:categoryId is null or c.id = :categoryId)
              and (:sourceType is null or s.sourceType = :sourceType)
              and (s.sourceType = com.daroring.fromdarori.domain.story.entity.StorySourceType.MANUAL
                   or rr.status = com.daroring.fromdarori.domain.residentreport.entity.ResidentReportStatus.APPROVED)
              and exists (select ns.id from NewsletterStory ns
                          where ns.story = s
                            and ns.newsletter.status = com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus.PUBLISHED)
            """,
            countQuery = """
            select count(s) from Story s
            left join s.residentReport rr
            where (:categoryId is null or s.category.id = :categoryId)
              and (:sourceType is null or s.sourceType = :sourceType)
              and (s.sourceType = com.daroring.fromdarori.domain.story.entity.StorySourceType.MANUAL
                   or rr.status = com.daroring.fromdarori.domain.residentreport.entity.ResidentReportStatus.APPROVED)
              and exists (select ns.id from NewsletterStory ns
                          where ns.story = s
                            and ns.newsletter.status = com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus.PUBLISHED)
            """)
    Page<Story> findPublishedAdminStories(
            @Param("categoryId") UUID categoryId,
            @Param("sourceType") StorySourceType sourceType,
            Pageable pageable
    );

    @Query(value = """
            select s from Story s
            join fetch s.category c
            left join s.residentReport rr
            where (:categoryId is null or c.id = :categoryId)
              and (:sourceType is null or s.sourceType = :sourceType)
              and (s.sourceType = com.daroring.fromdarori.domain.story.entity.StorySourceType.MANUAL
                   or rr.status = com.daroring.fromdarori.domain.residentreport.entity.ResidentReportStatus.APPROVED)
              and not exists (select ns.id from NewsletterStory ns
                              where ns.story = s
                                and ns.newsletter.status = com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus.PUBLISHED)
            """,
            countQuery = """
            select count(s) from Story s
            left join s.residentReport rr
            where (:categoryId is null or s.category.id = :categoryId)
              and (:sourceType is null or s.sourceType = :sourceType)
              and (s.sourceType = com.daroring.fromdarori.domain.story.entity.StorySourceType.MANUAL
                   or rr.status = com.daroring.fromdarori.domain.residentreport.entity.ResidentReportStatus.APPROVED)
              and not exists (select ns.id from NewsletterStory ns
                              where ns.story = s
                                and ns.newsletter.status = com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus.PUBLISHED)
            """)
    Page<Story> findUnpublishedAdminStories(
            @Param("categoryId") UUID categoryId,
            @Param("sourceType") StorySourceType sourceType,
            Pageable pageable
    );

    @Query("""
            select distinct s.id from Story s
            join NewsletterStory ns on ns.story = s
            where s.id in :storyIds
              and ns.newsletter.status = com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus.PUBLISHED
            """)
    Set<UUID> findPublishedStoryIds(@Param("storyIds") Collection<UUID> storyIds);
}
