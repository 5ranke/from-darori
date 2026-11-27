package com.daroring.fromdarori.domain.subscriber.entity;

import jakarta.persistence.*;
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
@Table(name = "subscriber")
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "name", length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriberStatus status;

    public static Subscriber subscribeNew(String email) {
        Subscriber subscriber = new Subscriber();
        subscriber.email = email;
        subscriber.status = SubscriberStatus.ACTIVE;
        return subscriber;
    }

    public static Subscriber subscribeAgain(Subscriber subscriber) {
        subscriber.status = SubscriberStatus.ACTIVE;
        subscriber.updatedAt = OffsetDateTime.now();
        return subscriber;
    }

    public static Subscriber unsubscribe(Subscriber subscriber) {
        subscriber.status = SubscriberStatus.UNSUBSCRIBED;
        subscriber.updatedAt = OffsetDateTime.now();
        return subscriber;
    }

    public static Subscriber subscribeByAdmin(String name, String email) {
        Subscriber subscriber = new Subscriber();
        subscriber.name = name;
        subscriber.email = email;
        subscriber.status = SubscriberStatus.ACTIVE;
        return subscriber;
    }

    public static Subscriber subscribeAgainByAdmin(String name, Subscriber subscriber) {
        subscriber.name = name;
        subscriber.status = SubscriberStatus.ACTIVE;
        subscriber.updatedAt = OffsetDateTime.now();
        return subscriber;
    }
}
