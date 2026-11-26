package com.daroring.fromdarori.domain.subscriber.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "unsubscriber")
public class Unsubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @Column(name = "detail", columnDefinition = "text")
    private String detail;

    @CreationTimestamp
    @Column(name = "unsubscribed_at", nullable = false, updatable = false)
    private OffsetDateTime unsubscribedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false, length = 30)
    private UnsubscribeReason reasonCode;

    public static Unsubscriber create(Subscriber subscriber, UnsubscribeReason reason_code,
                                      String detail) {
        Unsubscriber unsubscriber = new Unsubscriber();
        unsubscriber.subscriber = subscriber;
        unsubscriber.detail = detail;
        unsubscriber.reasonCode = reason_code;
        return unsubscriber;
    }
}
