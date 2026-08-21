CREATE TABLE board_pins (
    id UUID PRIMARY KEY,
    board_id UUID NOT NULL,
    pin_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bp_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_bp_pin FOREIGN KEY (pin_id) REFERENCES pins(id) ON DELETE CASCADE,
    CONSTRAINT uq_board_pin UNIQUE (board_id, pin_id)
);

CREATE INDEX idx_board_pin_board_id ON board_pins(board_id);
CREATE INDEX idx_board_pin_pin_id ON board_pins(pin_id);
