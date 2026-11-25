package com.daroring.fromdarori.domain.newsletter.repository;

import com.daroring.fromdarori.domain.newsletter.entity.NewsletterSchedule;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NewsletterScheduleRepository extends JpaRepository<NewsletterSchedule, UUID> {

    Optional<NewsletterSchedule> findBySingletonTrue();

    @Modifying
    @Query(value = """
            insert into newsletter_schedule (day_of_month, local_time, updated_by)
            values (:day, :time, :adminId)
            on conflict (singleton) do update
            set day_of_month = excluded.day_of_month,
                local_time = excluded.local_time,
                updated_by = excluded.updated_by,
                updated_at = clock_timestamp()
            """, nativeQuery = true)
    int upsert(short day, LocalTime time, UUID adminId);
}
