# Imagens e GIFs nas publicações

`POST /posts` continua aceitando JSON (`{"content":"Texto"}`) e agora também aceita `multipart/form-data`:

- `content`: texto opcional.
- `media`: um ou mais arquivos, repetindo o nome da parte para vários anexos.
- `tenor`: código HTML de incorporação ou link de um GIF do Tenor, opcional e incompatível com `media`.

A requisição exige sessão autenticada e o mesmo header CSRF usado nas demais escritas. Não defina o `Content-Type` manualmente ao enviar `FormData`: o cliente deve gerar o boundary.

## Regras iniciais

- Até 4 imagens PNG/JPEG ou apenas 1 GIF, sem misturar com imagens.
- Até 10 MiB por arquivo e 40 megapixels por imagem/quadro inicial.
- Texto é opcional quando há anexos. Texto em branco sem anexos é rejeitado.
- O servidor confere o formato real com ImageIO, o MIME declarado, as dimensões e a leitura do primeiro quadro antes de qualquer upload. A validação não analisa todos os quadros de GIFs.
- Os bytes são enviados ao bucket sem conversão, preservando animações.
- Comentários continuam aceitando apenas texto nesta etapa.

## Persistência e leitura

`PostMediaEntity` guarda a relação com a publicação, UUID público, chave do bucket, MIME, tamanho e posição. O conteúdo binário permanece no bucket, em `posts/{postId}/...`. Os anexos são consultados em lote pelo mapper, sem uma consulta de anexos por publicação.

Cada `PostResponse` inclui `media: [{mediaId,url,contentType,size,position,provider,externalId}]`, inclusive nas respostas de interações e reposts. Sem anexos, retorna `media: []`.

`GET /posts/{postId}/media/{mediaId}` entrega os bytes com o MIME registrado e exige autenticação. A mídia precisa pertencer ao post indicado. O campo `url` é relativo à API; no proxy Angular, acrescente `/api`.

Se a transação da publicação falhar depois de algum upload, o serviço tenta remover os arquivos enviados. Falhas nessa compensação são registradas para limpeza posterior. A API não aceita chaves ou URLs externas de mídia fornecidas pelo cliente.

## Front-end

O compositor permite selecionar arquivos locais, visualizar e remover anexos antes de publicar. Uma falha no envio preserva texto e seleção para uma nova tentativa. O componente compartilhado exibe as mídias no feed, nos perfis e na página da publicação, buscando blobs autenticados e liberando as URLs temporárias ao sair.

## Desenvolvimento

O perfil atual usa `ddl-auto: update`, que cria a tabela de anexos ao reiniciar a API. Em ambientes com migrações controladas, criar a tabela, índice e chave estrangeira antes de disponibilizar o fluxo. Os limites multipart estão em `application.yaml` (10MB por arquivo e 42MB por requisição).

## Incorporações do Tenor

O botão GIF oferece envio de `.gif` local e um campo para colar o HTML de incorporação ou link do Tenor. O exemplo `data-postid="15642784995767017938"` é suportado; o ID é tratado como string para preservar valores maiores que o limite seguro dos números JavaScript.

O parser aceita somente um ID numérico de até 20 dígitos dentro da incorporação ou de um link `https://tenor.com/view/...-{id}` / `https://tenor.com/embed/{id}`. O HTML e os scripts recebidos são descartados. Nenhuma URL fornecida é acessada pelo servidor.

Para esses anexos, `provider` é `TENOR`, `externalId` contém o ID, `url` aponta para `https://tenor.com/embed/{id}` e `size` é zero, pois não há arquivo enviado ao bucket. `storage_key` permite nulo e a entidade possui `tenor_id`. Mídias locais usam `provider: UPLOAD` e `externalId: null`.

O front monta um iframe com host fixo e ID validado, com sandbox; não usa `innerHTML` nem carrega `embed.js` na aplicação. A exibição depende da disponibilidade do Tenor e do GIF. Não se aceita misturar um Tenor com outros anexos no mesmo post. A API pública do Tenor não é necessária.

## Atualização das interações

Curtir, repostar e comentar atualizam os dados locais a partir da resposta da API. O perfil não refaz sua paginação a cada ação, comentários novos são inseridos na lista existente e os componentes de mídia dependem do ID estável do anexo, evitando novos downloads e reinício da animação a cada contador atualizado.


### Compatibilidade com bancos existentes

A inicialização SQL do perfil dev inclui `db/post-media-schema.sql`, que remove o `NOT NULL` antigo de `storage_key` de forma idempotente. O Hibernate `ddl-auto: update` cria `tenor_id`, mas não remove essa restrição de uma tabela existente. Sem essa migração, o banco rejeita um GIF do Tenor com erro de integridade. Em outros ambientes, aplique a mesma alteração por sua ferramenta de migrações.

## Editar e excluir publicações

- `PATCH /posts/{postId}` recebe JSON `{"content":"Texto atualizado"}` e retorna o `PostResponse`. Exige autenticação, CSRF e autoria; tentar alterar post de outra pessoa retorna 403. A edição mantém todos os anexos. Texto vazio só é permitido quando a publicação possui mídia.
- `DELETE /posts/{postId}` exige as mesmas verificações e retorna `{postId,parent}`. Quando a publicação é uma resposta, `parent` contém os contadores atualizados do post pai.
- A exclusão limpa o texto, marca `deleted_at` e remove os registros de mídia. Arquivos locais são removidos do bucket após a confirmação da transação; eventuais falhas de limpeza são registradas. Referências a GIFs externos são removidas sem acessar o Tenor.
- Feed, publicações, respostas, curtidas, consultas individuais e novas interações ignoram posts removidos. Respostas de outros autores são preservadas e passam a mostrar “Publicação removida.” no contexto do pai.
- O menu `⋯` aparece somente nos posts do autor autenticado. Editar mantém a mídia, excluir pede confirmação na própria interface e as listas são atualizadas sem recarregar a página. Ao excluir o post aberto, a aplicação navega ao feed.
- No ambiente dev, reinicie a API para criar a coluna `deleted_at` antes de usar as novas consultas.
