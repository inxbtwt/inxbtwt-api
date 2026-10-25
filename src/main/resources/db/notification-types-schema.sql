-- Mantém bancos existentes compatíveis com os novos eventos de amizade.
ALTER TABLE notification_entity
    DROP CONSTRAINT IF EXISTS notification_entity_type_check;

ALTER TABLE notification_entity
    ADD CONSTRAINT notification_entity_type_check
    CHECK (type IN ('POST_LIKED', 'POST_REPOSTED', 'POST_COMMENTED',
                    'FRIEND_REQUEST_RECEIVED', 'FRIEND_REQUEST_ACCEPTED', 'SYSTEM'));
