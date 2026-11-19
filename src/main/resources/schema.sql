BEGIN;

CREATE TABLE category
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code          VARCHAR(50) NOT NULL UNIQUE,
    name          VARCHAR(50) NOT NULL UNIQUE,
    display_order INT         NOT NULL UNIQUE CHECK (display_order >= 1)
);

INSERT INTO category (code, name, display_order)
VALUES ('VILLAGE_NEWS', '다로리의 요즘', 1),
       ('PROJECT_FOLLOW_UP', '프로젝트 후속', 2),
       ('RECRUITMENT', '구인/참여', 3),
       ('SEASONAL', '제철 정보', 4);

CREATE TABLE admin_user
(
    id                       UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    email                    VARCHAR(254) NOT NULL UNIQUE,
    password_hash            VARCHAR(255) NOT NULL,
    display_name             VARCHAR(50)  NOT NULL,
    role                     VARCHAR(20)  NOT NULL CHECK (role IN ('OPERATOR', 'REPRESENTATIVE')),
    refresh_token_hash       VARCHAR(255),
    refresh_token_expires_at TIMESTAMPTZ,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE resident_report
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    title          VARCHAR(80) NOT NULL,
    body           TEXT        NOT NULL CHECK (char_length(body) <= 3000),
    reporter_name  VARCHAR(100),
    reporter_email VARCHAR(254),
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED')),
    approved_by    UUID        REFERENCES admin_user (id) ON DELETE SET NULL,
    approved_at    TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE story
(
    id                   UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    source_type          VARCHAR(20) NOT NULL CHECK (source_type IN ('MANUAL', 'RESIDENT_REPORT')),
    category_id          UUID        NOT NULL REFERENCES category (id),
    created_by           UUID        REFERENCES admin_user (id) ON DELETE SET NULL,
    resident_report_id   UUID UNIQUE REFERENCES resident_report (id),
    title                VARCHAR(80) NOT NULL,
    body                 TEXT        NOT NULL CHECK (char_length(body) <= 3000),
    action_url           TEXT,
    application_deadline DATE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT story_source_report_check CHECK (
        (source_type = 'MANUAL' AND resident_report_id IS NULL) OR
        (source_type = 'RESIDENT_REPORT' AND resident_report_id IS NOT NULL)
        )
);

CREATE TABLE story_image
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    story_id      UUID NOT NULL REFERENCES story (id) ON DELETE CASCADE,
    object_key    TEXT NOT NULL UNIQUE,
    display_order INT  NOT NULL CHECK (display_order >= 1),
    UNIQUE (story_id, display_order)
);

CREATE TABLE newsletter
(
    id               UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    title            VARCHAR(80) NOT NULL,
    summary          VARCHAR(300),
    status           VARCHAR(20) NOT NULL DEFAULT 'SAVED' CHECK (status IN ('SAVED', 'SCHEDULED', 'SENDING', 'PUBLISHED', 'FAILED')),
    created_by       UUID        REFERENCES admin_user (id) ON DELETE SET NULL,
    scheduled_at     TIMESTAMPTZ UNIQUE,
    published_at     TIMESTAMPTZ,
    content_snapshot JSONB       NOT NULL,
    quiz_content     JSONB       NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE newsletter_story
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    newsletter_id  UUID  NOT NULL REFERENCES newsletter (id) ON DELETE CASCADE,
    story_id       UUID  REFERENCES story (id) ON DELETE SET NULL,
    display_order  INT   NOT NULL CHECK (display_order >= 1),
    category_id    UUID  REFERENCES category (id) ON DELETE SET NULL,
    story_snapshot JSONB NOT NULL,
    UNIQUE (newsletter_id, story_id),
    UNIQUE (newsletter_id, display_order)
);

CREATE TABLE subscriber
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    email      VARCHAR(254) NOT NULL UNIQUE,
    name       VARCHAR(100),
    status     VARCHAR(20)  NOT NULL CHECK (status IN ('ACTIVE', 'UNSUBSCRIBED')),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE newsletter_delivery
(
    id                  UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    newsletter_id       UUID        NOT NULL REFERENCES newsletter (id) ON DELETE RESTRICT,
    subscriber_id       UUID        NOT NULL REFERENCES subscriber (id) ON DELETE RESTRICT,
    status              VARCHAR(24) NOT NULL DEFAULT 'PENDING' CHECK (status IN
                                                                      ('PENDING', 'SENDING', 'ACCEPTED', 'DELIVERED',
                                                                       'RETRY_PENDING', 'FAILED', 'UNKNOWN',
                                                                       'SKIPPED')),
    attempt_count       INT         NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
    provider_message_id TEXT,
    last_attempt_at     TIMESTAMPTZ,
    next_retry_at       TIMESTAMPTZ,
    accepted_at         TIMESTAMPTZ,
    delivered_at        TIMESTAMPTZ,
    failure_reason      TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (newsletter_id, subscriber_id)
);

CREATE TABLE notice
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    title      VARCHAR(80) NOT NULL,
    category   VARCHAR(20) NOT NULL CHECK (category IN ('GUIDE', 'NEWS', 'PROGRAM', 'CLOSED')),
    body       TEXT        NOT NULL,
    is_pinned  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_by UUID        REFERENCES admin_user (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE unsubscriber
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    subscriber_id   UUID        NOT NULL REFERENCES subscriber (id) ON DELETE RESTRICT,
    reason_code     VARCHAR(30) NOT NULL CHECK (reason_code IN
                                                ('CONTENT_MISMATCH', 'NO_LONGER_INTERESTED', 'NOT_RECOGNIZED',
                                                 'OTHER')),
    detail          TEXT,
    unsubscribed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE newsletter_schedule
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    day_of_month SMALLINT    NOT NULL CHECK (day_of_month BETWEEN 1 AND 31),
    local_time   TIME        NOT NULL,
    updated_by   UUID        REFERENCES admin_user (id) ON DELETE SET NULL,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    singleton    BOOLEAN     NOT NULL DEFAULT TRUE CHECK (singleton),
    CONSTRAINT newsletter_schedule_singleton UNIQUE (singleton)
);

CREATE INDEX idx_story_category_created ON story (category_id, created_at DESC);
CREATE INDEX idx_story_created ON story (created_at DESC);
CREATE INDEX idx_resident_report_status_created ON resident_report (status, created_at DESC);
CREATE INDEX idx_newsletter_status_scheduled ON newsletter (status, scheduled_at);
CREATE INDEX idx_newsletter_story_story ON newsletter_story (story_id);
CREATE INDEX idx_newsletter_story_category ON newsletter_story (category_id);
CREATE INDEX idx_delivery_status_retry ON newsletter_delivery (status, next_retry_at);
CREATE INDEX idx_delivery_subscriber ON newsletter_delivery (subscriber_id);
CREATE INDEX idx_notice_pinned_created ON notice (is_pinned DESC, created_at DESC);
CREATE INDEX idx_unsubscriber_subscriber_time ON unsubscriber (subscriber_id, unsubscribed_at DESC);

INSERT INTO newsletter_schedule (day_of_month, local_time)
VALUES (10, TIME '10:00:00');

COMMIT;
