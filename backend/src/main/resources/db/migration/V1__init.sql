CREATE TABLE page (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    space_key VARCHAR(100),
    parent_page_id BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);

CREATE TABLE attachment (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(500) NOT NULL,
    content_type VARCHAR(200),
    size BIGINT,
    storage_path VARCHAR(1000) NOT NULL,
    description TEXT
);

CREATE TABLE page_attachment (
    id BIGSERIAL PRIMARY KEY,
    page_id BIGINT NOT NULL REFERENCES page(id) ON DELETE CASCADE,
    attachment_id BIGINT NOT NULL REFERENCES attachment(id) ON DELETE CASCADE,
    position INT NOT NULL
);

CREATE TABLE schedule (
    id BIGSERIAL PRIMARY KEY,
    page_id BIGINT NOT NULL REFERENCES page(id) ON DELETE CASCADE,
    scheduled_at TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'QUEUED',
    attempt_count INT DEFAULT 0,
    last_error TEXT
);

CREATE TABLE publish_log (
    id BIGSERIAL PRIMARY KEY,
    page_id BIGINT NOT NULL REFERENCES page(id) ON DELETE CASCADE,
    provider VARCHAR(200),
    remote_page_id VARCHAR(200),
    status VARCHAR(50),
    message TEXT,
    created_at TIMESTAMP DEFAULT now()
);