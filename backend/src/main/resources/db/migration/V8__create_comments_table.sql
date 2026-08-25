CREATE TABLE comments (
    id UUID PRIMARY KEY,
    pin_id UUID NOT NULL,
    user_id UUID NOT NULL,
    text VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_pin FOREIGN KEY (pin_id) REFERENCES pins(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_comments_pin_id ON comments(pin_id);
