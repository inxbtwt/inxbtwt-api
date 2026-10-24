-- GIFs externos do Tenor possuem tenor_id e não enviam arquivo ao bucket.
-- ddl-auto:update não remove NOT NULL de colunas existentes.
ALTER TABLE IF EXISTS post_media_entity
    ALTER COLUMN storage_key DROP NOT NULL;
