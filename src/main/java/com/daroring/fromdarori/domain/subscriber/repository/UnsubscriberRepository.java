package com.daroring.fromdarori.domain.subscriber.repository;

import com.daroring.fromdarori.domain.subscriber.entity.Subscriber;
import com.daroring.fromdarori.domain.subscriber.entity.Unsubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UnsubscriberRepository extends JpaRepository<Unsubscriber, UUID> {
    Unsubscriber findBySubscriber(Subscriber subscriber);
}
