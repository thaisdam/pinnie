CREATE TABLE pin_likes (
    id UUID PRIMARY KEY,
    pin_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pl_pin FOREIGN KEY (pin_id) REFERENCES pins(id) ON DELETE CASCADE,
    CONSTRAINT fk_pl_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_pin_like UNIQUE (pin_id, user_id)
);

CREATE INDEX idx_pin_likes_pin_id ON pin_likes(pin_id);
CREATE INDEX idx_pin_likes_user_id ON pin_likes(user_id);
