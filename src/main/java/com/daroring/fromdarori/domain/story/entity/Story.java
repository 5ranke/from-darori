package com.daroring.fromdarori.domain.story.entity;

import com.daroring.fromdarori.domain.admin.entity.AdminUser;
import com.daroring.fromdarori.domain.category.entity.Category;
import com.daroring.fromdarori.domain.residentreport.entity.ResidentReport;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "story")
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "created_by")
    private AdminUser createdBy;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "resident_report_id", unique = true)
    private ResidentReport residentReport;

    @Column(name = "body", nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "action_url", columnDefinition = "text")
    private String actionUrl;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private StorySourceType sourceType;

    @Column(name = "title", nullable = false, length = 80)
    private String title;
}
