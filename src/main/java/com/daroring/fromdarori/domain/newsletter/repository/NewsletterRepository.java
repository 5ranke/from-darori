package com.daroring.fromdarori.domain.newsletter.repository;

import com.daroring.fromdarori.domain.newsletter.entity.Newsletter;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterRepository extends JpaRepository<Newsletter, UUID> {

    List<Newsletter> findTop3ByStatusAndPublishedAtIsNotNullOrderByPublishedAtDescIdDesc(NewsletterStatus status);
}
