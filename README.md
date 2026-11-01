# inxbtwt-api

## Objetivo

Ser a API principal da rede social inxbtwt, responsável pelas regras de negócio, autorização das rotas e endpoints consumidos pelo front-end. A autenticação e emissão dos tokens de usuário são delegadas ao `inxbtwt-auth-api`.

## O que a API faz atualmente

| Área | Funcionalidades disponíveis |
| --- | --- |
| Autenticação | Encaminha login, logout e renovação ao `inxbtwt-auth-api`; mantém cookies HttpOnly, proteção CSRF, consulta do usuário e autorização por rota. Valida access tokens localmente com as chaves públicas do auth-api. |
| Usuários | Cadastro, consulta e atualização de dados, além da verificação de disponibilidade de username, e-mail e telefone. |
| Perfis | Consulta do próprio perfil, visualização por username e atualização de bio e capa. |
| Arquivos | Upload e consulta de fotos de perfil e capa, com armazenamento compatível com S3. |
| Publicações | Criação, edição e exclusão de posts, anexos de imagem ou GIF e incorporação de conteúdo do Tenor. |
| Interações | Comentários, curtidas e reposts. |
| Feed | Listagem paginada de publicações com estratégia cronológica. |
| Amizades | Envio, aceite, recusa e cancelamento de solicitações, consulta de relações, listagem de amigos e encerramento de amizades. |
| Notificações | Consulta de notificações, contagem de não lidas e marcação de leitura. |
| Busca | Validação da requisição e encaminhamento da pesquisa de usuários ao microsserviço especializado. |

A aplicação usa Java 21 e Spring Boot. Os dados são persistidos em PostgreSQL, e os arquivos são armazenados por uma integração compatível com S3. A API principal mantém a estrutura das tabelas e os scripts de índices do banco compartilhado.

## Integração com microsserviços

A pesquisa de usuários é executada pelo [inxbtwt-search-api](https://github.com/inxbtwt/inxbtwt-search-api). A documentação desse repositório explica como iniciar o serviço, sua conexão com o banco e o funcionamento da consulta.

O fluxo da busca é:

```text
Front-end → API principal → OpenFeign + token de serviço → Microsserviço de busca → PostgreSQL
```

O front-end chama a API principal com uma sessão autenticada:

```http
GET /search?user=luc&page=0&size=20
```

A API principal valida o termo e a paginação, obtém um access token `client_credentials` com escopo `search:read` do auth-api e usa OpenFeign para chamar `GET /api/search`. O search-api valida assinatura, issuer, audiência e escopo antes de executar a pesquisa. O JWT de usuário em cookie não é repassado ao search-api.

O auth-api verifica a senha na tabela compartilhada `user_data_entity` e armazena refresh tokens de uso único em `refresh_token_entity`. Os endereços dos serviços e das chaves públicas estão fixos nos arquivos `application*.yaml`; os perfis `dev` e `local` usam `localhost`, e o perfil `docker` usa os nomes internos dos serviços. O cliente de serviço usa uma credencial fixa de desenvolvimento, compartilhada entre os três projetos.

O microsserviço consulta a tabela de usuários no mesmo banco e schema utilizados pela API principal. A pesquisa considera trechos do username, sem diferenciar maiúsculas de minúsculas, e pode aproveitar o índice GIN de trigramas criado com `pg_trgm`.

Os resultados são devolvidos pela API principal em `data.content`, com nome, username e URL da foto, acompanhados dos metadados de paginação. O contrato atual pode incluir o próprio usuário autenticado e não retorna UUID nos itens da busca. A consulta do perfil completo por username continua na API principal.

Quando o front é acessado pela Vercel, a chamada é feita para `/api/search`; o proxy encaminha a requisição para `/search` na API principal. A consulta ao banco para produzir os resultados da busca fica no microsserviço.

## Estado atual

Usuários, perfis, publicações, feed, amizades e notificações continuam implementados nesta API. A autenticação de usuários foi transferida ao auth-api; esta API mantém os endpoints compatíveis com o front-end e as regras de acesso às rotas. A busca é delegada ao microsserviço por OpenFeign. O antigo endpoint de pesquisa `GET /profiles` foi removido; as rotas de consulta e atualização de perfil permanecem disponíveis.

A separação dos serviços está sendo feita gradualmente. Atualmente, a pesquisa considera apenas username; busca por nome, posts e similaridade para erros de digitação ainda não faz parte do fluxo implementado.
