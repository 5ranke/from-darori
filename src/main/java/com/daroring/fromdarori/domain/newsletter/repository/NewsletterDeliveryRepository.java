package com.daroring.fromdarori.domain.newsletter.repository;

import com.daroring.fromdarori.domain.newsletter.entity.NewsletterDelivery;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.time.OffsetDateTime;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
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

    @Query("""
            select d.id from NewsletterDelivery d
            where d.status = PENDING or (d.status = RETRY_PENDING and d.nextRetryAt <= :now)
            order by d.createdAt, d.id
            """)
    List<UUID> findReadyIds(OffsetDateTime now, Pageable page);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from NewsletterDelivery d where d.id = :id")
    Optional<NewsletterDelivery> findForUpdate(UUID id);

    @Modifying
    @Query("""
            update NewsletterDelivery d set d.status = UNKNOWN, d.failureReason = 'Attempt interrupted',
                d.updatedAt = :now
            where d.status = SENDING and d.lastAttemptAt < :cutoff
            """)
    int markInterruptedAttempts(OffsetDateTime cutoff, OffsetDateTime now);
}
