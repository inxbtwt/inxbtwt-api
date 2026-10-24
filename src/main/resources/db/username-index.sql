CREATE UNIQUE INDEX IF NOT EXISTS uk_user_data_username_lower
    ON user_data_entity (lower(username));

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_data_email_lower
    ON user_data_entity (lower(email));

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_data_phone_digits
    ON user_data_entity (regexp_replace(phone, '[^0-9]', '', 'g'));
