# Feed de publicações

## Endpoints

Todas as rotas exigem uma sessão autenticada. As operações de escrita também exigem o header CSRF, como os demais controllers.

- `POST /posts`: cria uma publicação de texto. O autor vem da sessão; o cliente envia somente `{"content":"Minha publicação"}`.
- `GET /posts?authorId={userId}&page=0&size=20`: consulta somente as publicações do autor, da mais recente para a mais antiga, com desempate por ID. Retorna `content`, `page`, `size`, `hasNext` e `hasPrevious`, sem aplicar recomendações.
- `GET /posts/{postId}`: consulta uma publicação pelo UUID público.
- `GET /feed?page=0&size=20`: usa a estratégia padrão.
- `GET /feed?strategy=chronological&page=0&size=20`: seleciona explicitamente uma estratégia.

A resposta segue `ApiResponse`. O `data` do feed contém `strategy`, `content`, `page`, `size`, `hasNext` e `hasPrevious`. Cada publicação contém UUID, texto, autor (UUID, nome, username e URL da foto) e datas. IDs internos, e-mail, telefone e senha não são retornados.

A página começa em zero. O tamanho permitido é de 1 a 100. Sem publicações, `content` fica vazio. Uma estratégia desconhecida ou paginação inválida retorna HTTP 400; publicação inexistente retorna HTTP 404.

## Extensão das recomendações

`FeedService` é a interface de entrada da operação. `FeedServiceImpl` autentica o usuário, valida a paginação, resolve a estratégia e mapeia os posts para a resposta, preservando a ordem recebida.

`FeedStrategy` é o contrato de cada algoritmo ou versão:

1. Crie uma implementação e registre-a como bean Spring (`@Component` ou `@Service`).
2. Retorne uma chave única e estável em `key()`, por exemplo `recommended-v1` ou `recommended-v2`. Não é necessário alterar um enum ou adicionar condições ao controller.
3. Implemente `getFeed(FeedContext context)`. O contexto fornece o UUID do usuário autenticado, a página e o tamanho. Use esse UUID para obter os sinais necessários à recomendação.
4. Selecione e ordene as publicações dentro da estratégia. Retorne um `Slice<PostEntity>` com a ordem final e o indicador correto de continuação. Ao consultar o repositório, carregue também o autor, por exemplo com `@EntityGraph(attributePaths = "author")`, evitando uma consulta por publicação.
5. Para usar a nova versão como padrão, altere `app.feed.default-strategy` na configuração. Também é possível selecioná-la pelo parâmetro `strategy`.

O registro é montado automaticamente a partir das implementações disponíveis. Chaves vazias ou duplicadas e uma estratégia padrão não registrada impedem a inicialização, evitando uma escolha ambígua do algoritmo.

## Primeira implementação

`ChronologicalFeedStrategy` fornece um feed global para usuários autenticados, incluindo publicações do próprio usuário. Busca as publicações por data de criação decrescente e usa o ID interno decrescente como desempate. A seleção e a paginação são feitas no banco; os autores são carregados na mesma consulta.

O feed usa `Slice` para indicar a próxima página sem exigir a contagem de todas as publicações. Outras recomendações podem usar consultas e ordenações diferentes, mantendo o contrato HTTP.

A base atual suporta publicações de texto. Relações de seguidores, mídia, curtidas e sinais de recomendação podem ser adicionados nas camadas correspondentes, sem substituir a interface do feed.
