# API JUST

## Desenvolvimento local

O `compose.yaml` inicia somente PostgreSQL e SeaweedFS. Execute a API pela IDE ou pelo Maven, usando o profile Spring `local` ou `dev`.

```sh
docker compose up -d --wait
```

Para encerrar as ferramentas:

```sh
docker compose down
```

## Testar o front na Vercel

O Dockerfile compila a API com Java 21 e Maven Wrapper e gera uma imagem final com JRE 21, executada por um usuário sem privilégios. O Compose usa a imagem `inxbtwt-api:local-test` por padrão e ativa o profile Spring `local`.

O arquivo `compose.local.yaml` sobe a API e o Cloudflare Quick Tunnel junto com PostgreSQL e SeaweedFS. O túnel aponta diretamente para `http://api:8081` na rede Docker. Não é necessário executar Java ou Maven no host.

1. Crie um arquivo `.env` na raiz e configure `APP_SECURITY_CORS_ALLOWED_ORIGINS` para a origem HTTPS exata do front na Vercel. Para permitir também o front de desenvolvimento, separe as origens por vírgula. O arquivo `.env` não é versionado.
2. Compile a imagem e suba os serviços:

```sh
docker compose -f compose.local.yaml up -d --build --wait
```

3. Consulte a URL pública gerada:

```sh
docker compose -f compose.local.yaml logs -f tunnel
```

4. Copie a origem `https://….trycloudflare.com` para `BACKEND_TUNNEL_URL`, no GitHub Environment **local** do repositório `w-just`, e execute o deploy do front. Não inclua `/api`. A Vercel encaminha `/api/*` ao túnel e ajusta os cookies de autenticação.

A URL do túnel é temporária e pode mudar quando ele for recriado ou reiniciado. Nesse caso, atualize a variável e refaça o deploy do front.

## Imagem e configuração

O arquivo `.env` aceita `API_IMAGE_TAG` para identificar a versão, por exemplo `local-test-002`. Depois de alterar código ou tag, use o mesmo comando com `--build` para gerar e executar a nova imagem. Ela é criada no Docker local; não é publicada em um registry.

O Compose substitui os endereços de banco e storage pelos nomes dos serviços (`postgres:5432` e `seaweedfs:8333`). As configurações existentes do profile Spring `local`, incluindo as credenciais de desenvolvimento, continuam sendo usadas. O serviço `api-db-init` cria o schema `api_local_test` de forma idempotente; a API de testes usa esse schema para não depender da estrutura antiga de `public`. Os usuários e posts dessa versão começam vazios. Os dados existentes em `public` são preservados, assim como os volumes do banco e do storage entre recriações dos containers.

A API fica acessível no host em `http://localhost:8081`. Encerre uma API iniciada pela IDE antes de subir o container para evitar disputa dessa porta. Para verificar a resposta local, use `GET /auth/csrf`; pela Vercel, a rota correspondente é `/api/auth/csrf`.

```sh
docker compose -f compose.local.yaml ps
docker compose -f compose.local.yaml logs --tail 100 api
```

Para encerrar apenas a exposição pública:

```sh
docker compose -f compose.local.yaml stop tunnel
```

Os dois arquivos usam o mesmo projeto Compose e os mesmos volumes. Para alternar do modo Vercel para desenvolvimento, encerre primeiro os containers com `docker compose -f compose.vercel.yaml down` e depois execute `docker compose up -d --wait`. Isso libera a porta 8081 para a IDE e encerra o túnel. Não use `down -v` se quiser preservar os dados.

O Quick Tunnel é destinado a testes. Não há reinício automático configurado para ele, pois um novo processo pode gerar outra URL pública.

Referências: [Cloudflare Quick Tunnels](https://developers.cloudflare.com/tunnel/get-started/quick-tunnels/) e [Docker multi-stage builds](https://docs.docker.com/get-started/docker-concepts/building-images/multi-stage-builds/).
