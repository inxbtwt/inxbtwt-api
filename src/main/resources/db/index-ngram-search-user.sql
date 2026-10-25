CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_user_data_username_trgm
    ON user_data_entity
    USING GIN (username gin_trgm_ops);