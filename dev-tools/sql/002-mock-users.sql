-- PostgreSQL: cria até 200 usuários fictícios e seus perfis.
-- Uso manual em desenvolvimento; não é executado automaticamente pela aplicação.
-- Senha de todas as contas: SenhaTeste123 (hash PBKDF2 compatível com a API).
-- Exemplo de login: mock_lucas_001 ou mock_lucas_001@example.com.
-- Reexecutar ignora usuários que conflitem com username, e-mail ou telefone existentes.
-- Nenhum registro existente é alterado. Foto e capa ficam vazias.

BEGIN;

WITH settings AS (
    SELECT
        ARRAY[
            'Lucas', 'Ana', 'Bruno', 'Carla', 'Diego',
            'Elisa', 'Felipe', 'Gabriela', 'Henrique', 'Isabela',
            'Joao', 'Julia', 'Leonardo', 'Mariana', 'Nicolas',
            'Olivia', 'Pedro', 'Rafaela', 'Thiago', 'Vitoria'
        ] AS names,
        ARRAY[
            'Silva', 'Santos', 'Oliveira', 'Souza', 'Lima',
            'Costa', 'Pereira', 'Almeida', 'Ferreira', 'Rodrigues'
        ] AS surnames,
        '{pbkdf2}03a2e5f42c40dedd496f827835b0b4bc989929fe880dcf9809aeeb598e329f3837e938a77bddde9a35f53fabf76cb997' AS password_hash
), mock_users AS (
    SELECT
        n,
        names[((n - 1) % 20) + 1] || ' ' || surnames[((n - 1) / 20) + 1] AS name,
        'mock_' || LOWER(names[((n - 1) % 20) + 1]) || '_' || LPAD(n::text, 3, '0') AS username,
        '11990' || LPAD(n::text, 6, '0') AS phone,
        password_hash
    FROM settings
    CROSS JOIN generate_series(1, 200) AS numbers(n)
), inserted_users AS (
    INSERT INTO user_data_entity (
        user_id,
        name,
        username,
        email,
        phone,
        password,
        photo_url,
        cover_url,
        created_at,
        updated_at
    )
    SELECT
        gen_random_uuid(),
        name,
        username,
        username || '@example.com',
        phone,
        password_hash,
        NULL,
        NULL,
        CURRENT_TIMESTAMP - (n * INTERVAL '1 day'),
        CURRENT_TIMESTAMP
    FROM mock_users
    ON CONFLICT DO NOTHING
    RETURNING id, name, username
), inserted_profiles AS (
    INSERT INTO profile_entity (user_id, bio, cover_url)
    SELECT
        id,
        'Perfil fictício de ' || name || '. Criado para desenvolvimento.',
        NULL
    FROM inserted_users
    RETURNING id
)
SELECT
    (SELECT COUNT(*) FROM inserted_users) AS usuarios_criados,
    (SELECT COUNT(*) FROM inserted_profiles) AS perfis_criados;

COMMIT;

-- Conferência manual (somente usuários criados por este script):
-- SELECT user_id, name, username, email, phone
-- FROM user_data_entity
-- WHERE username LIKE 'mock\_%' ESCAPE '\'
--   AND email = username || '@example.com'
-- ORDER BY username;
