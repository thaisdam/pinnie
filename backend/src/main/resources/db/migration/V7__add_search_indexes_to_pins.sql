CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_pins_title_trgm ON pins USING GIN (title gin_trgm_ops);
CREATE INDEX idx_pins_description_trgm ON pins USING GIN (description gin_trgm_ops);
