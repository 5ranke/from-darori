package com.daroring.fromdarori.domain.subscriber.repository;

import com.daroring.fromdarori.domain.subscriber.entity.Subscriber;
import com.daroring.fromdarori.domain.subscriber.entity.SubscriberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriberRepository extends JpaRepository<Subscriber, UUID> {
    Optional<Subscriber> findByEmail(String email);
    Page<Subscriber> findByStatus(SubscriberStatus status, Pageable pageable);
}
