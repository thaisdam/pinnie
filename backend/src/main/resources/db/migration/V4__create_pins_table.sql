CREATE TABLE pending_image_uploads (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    size BIGINT NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_pending_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE pins (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(100),
    description TEXT,
    link VARCHAR(2048),
    alt_text VARCHAR(255),
    media_stored_filename VARCHAR(255) NOT NULL,
    media_content_type VARCHAR(50) NOT NULL,
    media_size BIGINT NOT NULL,
    media_width INTEGER NOT NULL,
    media_height INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pins_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_pins_user_id ON pins(user_id);
CREATE INDEX idx_pending_user_id ON pending_image_uploads(user_id);
