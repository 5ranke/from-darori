package com.daroring.fromdarori.domain.newsletter.mail;

import java.util.UUID;

public interface NewsletterMailSender {

    String send(UUID deliveryId, String recipient, NewsletterMailContent content) throws SendFailure;

    default String recipientFilter() {
        return null;
    }

    enum FailureKind {
        RETRYABLE, PERMANENT, UNKNOWN
    }

    class SendFailure extends Exception {
        private final FailureKind kind;

        public SendFailure(FailureKind kind, String reason) {
            super(reason);
            this.kind = kind;
        }

        public FailureKind kind() {
            return kind;
        }
    }
}
