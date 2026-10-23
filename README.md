# inxbtwt-api

API com Java 21, Spring Boot, Spring Data JPA/Hibernate, PostgreSQL e S3 via SeaweedFS.

## Endpoints de usuários

As respostas usam `ApiResponse` (`message`, `data`, `status`). O identificador público é o UUID `userId` retornado na criação.

| Método | Endpoint | Uso |
| --- | --- | --- |
| POST | `/users` | Criação; retorna HTTP 201 |
| GET | `/users/{userId}` | Consulta individual |
| GET | `/users?page=0&size=20` | Listagem paginada; tamanho entre 1 e 100 |
| PUT | `/users/{userId}` | Substitui nome, URL da foto, e-mail, telefone e senha; todos obrigatórios |
| PATCH | `/users/{userId}` | Atualiza apenas os campos não nulos enviados |

GET, PUT e PATCH retornam HTTP 200. Usuário inexistente retorna 404; dados inválidos, 400; e-mail usado por outro usuário, 409. Senhas não são incluídas na resposta.

Exemplo de corpo PATCH: `{"name":"Novo Nome"}`. Campos omitidos ou nulos são preservados; objeto vazio ou apenas com nulos é rejeitado. UUID e datas são gerenciados pelo servidor. A atualização preserva a identidade e a data de criação, e atualiza `updatedAt`.

## Iniciar a API

Na pasta `inxbtwt-api`, inicie o banco e o armazenamento:

```powershell
docker compose up -d --wait postgres seaweedfs
```

Inicie a API:

```powershell
.\mvnw.cmd spring-boot:run
```

O perfil padrão é `dev`. Para selecioná-lo explicitamente:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

A API usa `http://localhost:8080`. O banco é `rsdb`, em `localhost:5432`, com usuário `admin` e senha `admin`. Os valores estão diretamente em `compose.yaml` e `src/main/resources/application-dev.yml`, sem `.env` ou interpolação de variáveis. As configurações `POSTGRES_*` no Compose são parâmetros exigidos pela imagem oficial, com valores literais.

O Compose é iniciado manualmente. O serviço Grafana existente continua disponível por `docker compose up -d grafana-lgtm`.

## S3 local com SeaweedFS

O SeaweedFS 4.48 roda em um único container no modo `mini`, com bucket criado automaticamente. Não exige conta, token externo, `.env` ou configuração no terminal. As credenciais locais ficam fixas no Compose e no perfil `dev`.

Defina os buckets no arquivo `dev-tools/buckets.txt`, um por linha. O Compose lê essa lista ao iniciar o SeaweedFS. Após editar, aplique com `docker compose up -d --wait --force-recreate seaweedfs`. Os buckets existentes e seus arquivos são preservados.

| Propriedade | Valor |
| --- | --- |
| Endpoint | `http://localhost:8333` |
| Região | `us-east-1` |
| Access key / secret key | `test` / `test` |
| Bucket | `inxbtwt-files` |
| Endereçamento | Path-style |
| Volume persistente | `seaweedfs-data`, montado em `/data` |

Os parâmetros `AWS_*` do container têm valores literais no Compose. Não é necessário fornecer credenciais AWS reais. Apenas a porta S3 é publicada, em loopback.

O SDK AWS Java 2 disponibiliza `S3Client` e `S3Presigner` no perfil `dev`. O `BucketService`, implementado por `S3Service`, envia e busca arquivos. O cliente não exige que o S3 esteja online apenas para inicializar a API.

Com os containers iniciados, execute a integração real de upload, download, URL pré-assinada e exclusão do objeto de teste:

```powershell
.\mvnw.cmd test "-Dtest=S3StorageIT"
```

O teste de integração é explícito; `mvnw test` continua executando os testes convencionais. Para parar sem perder os arquivos, use `docker compose down` sem a opção `-v`.

Referência: [SeaweedFS Quick Start](https://github.com/seaweedfs/seaweedfs#quick-start).
## Persistência e ORM

- PostgreSQL 18, com dados persistidos no volume `postgres-data`.
- Spring Data JPA usa Hibernate e o driver PostgreSQL já declarados no Maven. O dialeto é detectado pela conexão.
- Em `dev`, `ddl-auto: update` cria e atualiza tabelas conforme as entidades JPA forem adicionadas, preservando os dados existentes. Não substitui migrations de produção.
- SQL e formatação habilitados; Open Session in View desabilitado; timestamps JDBC em UTC.
- Ainda não há entidades de domínio, portanto nenhuma tabela de negócio é criada nesta etapa.

Para testar com o banco iniciado:

```powershell
.\mvnw.cmd test
```

Para parar os serviços preservando os dados:

```powershell
docker compose down
```

## Cadastro de usuario

`POST /users` recebe apenas o objeto JSON no body, com `Content-Type: application/json`:

```json
{"name":"Lucas","email":"lucas@example.com","phone":"11999999999","password":"password123"}
```

```powershell
curl.exe -X POST http://localhost:8080/users -H "Content-Type: application/json" --data-binary "@user.json"
```

Retorna `201` com `ApiResponse<UserResponse>`. O cadastro nao acessa o bucket. As URLs de perfil e capa ficam nulas ate os uploads.

Envie arquivos separadamente, depois de obter o UUID:

```powershell
curl.exe -X POST "http://localhost:8080/users/UUID_DO_USUARIO/photo" -F "photo=@avatar.png"
curl.exe -X POST "http://localhost:8080/users/UUID_DO_USUARIO/photo?type=COVER" -F "photo=@capa.png"
```

Os uploads aceitam PNG, JPEG e WEBP e retornam `200` com a URL, chave e metadados em ApiResponse. As fotos usam o caminho configurado e UUID v7.

## URLs de perfil e capa

`GET /users/{userId}/photos` retorna as duas URLs atuais:

```json
{"message":"URLs das fotos do usuario consultadas com sucesso.","status":"OK","data":{"userId":"UUID","profileUrl":"http://localhost:8333/inxbtwt-files/img/profile/photo/UUID_V7","coverUrl":null}}
```

Perfil e capa sao opcionais e ficam em `photo_url` e `cover_url`. Envie a parte `photo` para `POST /users/{userId}/photo?type=COVER` para defini-la. O tipo padrao e `PROFILE`; trocar um tipo preserva o outro. Sao retornadas as URLs atuais, sem historico de uploads.

`GET /users/{userId}/photo?url=URL_CODIFICADA` busca a imagem pela URL de perfil ou capa associada ao usuario. Retorna os bytes com o Content-Type da imagem, sem envelope ApiResponse. URLs de outros usuarios retornam 404; objetos removidos do bucket retornam 404. A busca usa o SDK S3 e aceita apenas URLs do endpoint e bucket configurados.

```powershell
curl.exe -X POST "http://localhost:8080/users/UUID_DO_USUARIO/photo?type=COVER" -F "photo=@capa.png"
curl.exe "http://localhost:8080/users/UUID_DO_USUARIO/photos"
curl.exe --get "http://localhost:8080/users/UUID_DO_USUARIO/photo" --data-urlencode "url=http://localhost:8333/inxbtwt-files/img/profile/photo/UUID_V7" --output foto.png
```

Reinicie a API para carregar o cadastro JSON. Em bancos existentes, aplique `dev-tools/sql/001-user-photo-optional.sql` uma vez para remover o NOT NULL de `photo_url` (ja aplicado no banco local desta workspace). Bancos novos seguem o mapeamento JPA, com as fotos opcionais.
