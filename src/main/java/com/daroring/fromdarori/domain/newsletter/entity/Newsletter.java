package com.daroring.fromdarori.domain.newsletter.entity;

import com.daroring.fromdarori.domain.admin.entity.AdminUser;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "newsletter")
public class Newsletter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "created_by")
    private AdminUser createdBy;

    @Column(name = "scheduled_at", unique = true)
    private OffsetDateTime scheduledAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_snapshot", nullable = false, columnDefinition = "jsonb")
    private String contentSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quiz_content", nullable = false, columnDefinition = "jsonb")
    private String quizContent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "title", nullable = false, length = 80)
    private String title;

    @Column(name = "summary", length = 300)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NewsletterStatus status = NewsletterStatus.SAVED;
}
