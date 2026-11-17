package com.daroring.fromdarori.domain.admin.repository;

import com.daroring.fromdarori.domain.admin.dto.AdminDashboardResponse.RecentStory;
import com.daroring.fromdarori.domain.admin.dto.AdminDashboardResponse.Statistics;
import com.daroring.fromdarori.domain.story.entity.StorySourceType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminDashboardRepository {
    private final JdbcClient jdbcClient;

    public Statistics statistics() {
        return jdbcClient.sql("""
                SELECT
                    (SELECT COUNT(*) FROM story s WHERE NOT EXISTS (
                        SELECT 1 FROM newsletter_story ns WHERE ns.story_id = s.id
                    )) AS story_count,
                    (SELECT COUNT(*) FROM subscriber WHERE status = 'ACTIVE') AS subscriber_count,
                    (SELECT COUNT(*) FROM newsletter WHERE status = 'PUBLISHED') AS published_newsletter_count,
                    (SELECT COUNT(*) FROM resident_report WHERE status = 'PENDING') AS pending_report_count
                """).query((rs, rowNum) -> new Statistics(rs.getLong("story_count"),
                        rs.getLong("subscriber_count"), rs.getLong("published_newsletter_count"),
                        rs.getLong("pending_report_count"))).single();
    }

    public List<RecentStory> recentStories() {
        return jdbcClient.sql("""
                SELECT s.id, s.title, s.category_id, c.name AS category_name, s.source_type, s.created_at
                FROM story s JOIN category c ON c.id = s.category_id
                ORDER BY s.created_at DESC, s.id DESC
                LIMIT 3
                """).query((rs, rowNum) -> new RecentStory(rs.getObject("id", UUID.class),
                        rs.getString("title"), rs.getObject("category_id", UUID.class),
                        rs.getString("category_name"), StorySourceType.valueOf(rs.getString("source_type")),
                        rs.getObject("created_at", OffsetDateTime.class))).list();
    }
}
