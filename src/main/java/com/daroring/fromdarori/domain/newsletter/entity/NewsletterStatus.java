package com.daroring.fromdarori.domain.newsletter.entity;

import java.util.Set;

public enum NewsletterStatus {
    SAVED, SCHEDULED, SENDING, PUBLISHED, FAILED;

    public static final Set<NewsletterStatus> HOLDING_STORIES = Set.of(SAVED, SCHEDULED, SENDING, PUBLISHED);
}
