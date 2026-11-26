package com.daroring.fromdarori.domain.newsletter.repository;

import com.daroring.fromdarori.domain.newsletter.entity.NewsletterDelivery;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NewsletterDeliveryRepository extends JpaRepository<NewsletterDelivery, UUID> {

    @Modifying
    @Query(value = """
            insert into newsletter_delivery (newsletter_id, subscriber_id)
            select :newsletterId, s.id from subscriber s where s.status = 'ACTIVE'
            on conflict (newsletter_id, subscriber_id) do nothing
            """, nativeQuery = true)
    int enqueueActiveSubscribers(UUID newsletterId);
}
