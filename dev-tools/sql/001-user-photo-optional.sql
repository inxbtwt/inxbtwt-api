-- Permite cadastrar o usuario antes de enviar a foto.
ALTER TABLE IF EXISTS user_data_entity ALTER COLUMN photo_url DROP NOT NULL;
