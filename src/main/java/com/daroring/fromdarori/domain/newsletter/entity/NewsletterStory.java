package com.daroring.fromdarori.domain.newsletter.entity;

import com.daroring.fromdarori.domain.category.entity.Category;
import com.daroring.fromdarori.domain.story.entity.Story;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "newsletter_story", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"newsletter_id", "story_id"}),
        @UniqueConstraint(columnNames = {"newsletter_id", "display_order"})
})
public class NewsletterStory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "newsletter_id", nullable = false)
    private Newsletter newsletter;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "story_id")
    private Story story;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "category_id")
    private Category category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "story_snapshot", nullable = false, columnDefinition = "jsonb")
    private String storySnapshot;
}
