package com.daroring.fromdarori.domain.newsletter.entity;

import com.daroring.fromdarori.domain.admin.entity.AdminUser;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "newsletter_schedule", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"singleton"})
})
public class NewsletterSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "day_of_month", nullable = false)
    private Short dayOfMonth;

    @Column(name = "local_time", nullable = false)
    private LocalTime localTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "updated_by")
    private AdminUser updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "singleton", nullable = false)
    private Boolean singleton = true;
}
