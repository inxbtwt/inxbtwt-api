# Base de notificações

`NotificationService` é a entrada interna de envio. `send(command)` usa o site; `send(command, channel)` seleciona explicitamente `IN_APP` ou `EMAIL`.

## Separação por canal

`NotificationSender` abstrai a entrega. `NotificationServiceImpl` valida o comando e descobre as implementações registradas como beans Spring, usando o valor retornado por `channel()`. Canais nulos ou duplicados impedem a inicialização.

`InAppNotificationSender` é a implementação inicial: resolve destinatário, autor opcional e publicação opcional e persiste `NotificationEntity` em uma transação. As relações usam os IDs internos no banco; o comando usa UUIDs públicos. O comando é interno, não um contrato HTTP que permita ao cliente escolher livremente quem notificar.

`EMAIL` está reservado no contrato. Para ativá-lo, crie uma implementação de `NotificationSender` que retorne `EMAIL`, resolva o endereço a partir de `recipientId` e delegue o envio a um provedor configurado. Não há SMTP configurado nem envio de e-mail simulado: solicitar um canal sem implementação produz erro explícito.

Notificações no site e e-mails têm ciclos de vida diferentes. A caixa de entrada não representa uma fila de e-mails; marcar uma notificação como lida não confirma entrega ou leitura de e-mail. Ao implementar envio externo, dispare depois da confirmação da ação de origem e use outbox/fila se for necessário garantir retentativas. O despachante atual é síncrono e seleciona um canal por chamada.

## Entidade

- `notificationId`: UUID público, além do ID interno.
- `recipient`: destinatário obrigatório.
- `actor`: usuário que originou a ação, opcional para avisos do sistema.
- `post`: publicação relacionada, opcional.
- `type`: `POST_LIKED`, `POST_REPOSTED`, `POST_COMMENTED`, `FRIEND_REQUEST_RECEIVED`, `FRIEND_REQUEST_ACCEPTED` ou `SYSTEM`.
- `title`, `message`, `createdAt` e `readAt`.

`readAt` nulo significa não lida. `markAsRead()` é idempotente e preserva a primeira data de leitura. O repositório oferece paginação cronológica por destinatário, contagem de não lidas e consulta por UUID junto do destinatário, para as futuras operações autenticadas. A entidade não deve ser serializada diretamente em controllers.

## Interações com publicações

`PostInteractionServiceImpl` usa `NotificationService` para entregar no canal padrão `IN_APP`:

- Curtir e repostar notificam o autor somente quando a inserção realmente cria uma relação. Repetir PUT sem desfazer a relação não duplica avisos.
- Descurtir e desfazer repost não criam notificações e não apagam o histórico anterior. Refazer uma interação desfeita representa uma nova ação e gera um novo aviso.
- Cada novo comentário notifica o autor da publicação respondida; respostas a comentários notificam o autor do comentário pai, sem notificar automaticamente toda a conversa.
- Interações do autor com a própria publicação não geram avisos.
- O ator vem da sessão. O destinatário vem do autor da publicação, nunca de um UUID recebido como destinatário no request.

A persistência da notificação do site participa da mesma transação da interação. Se a ação ou a gravação do aviso falhar, ambas são revertidas. E-mail não deve ser enviado dentro dessa transação: uma futura integração deverá publicar depois do commit, preferencialmente com outbox/fila quando houver necessidade de entrega garantida.

## Caixa de entrada autenticada

`NotificationInboxService` separa as operações da caixa de entrada do contrato de entrega por canal. Todas as consultas usam o usuário da sessão:

- `GET /notifications?page=0&size=20`: retorna `content`, `page`, `size`, `hasNext` e `hasPrevious`, da mais recente à mais antiga. Tamanho entre 1 e 100.
- `GET /notifications/unread-count`: retorna `{count}`.
- `PATCH /notifications/{notificationId}/read`: marca uma notificação própria como lida e retorna seus dados. A data inicial de leitura é preservada por uma atualização condicional no banco.
- `PATCH /notifications/read-all`: marca apenas as não lidas do usuário autenticado.

As rotas de escrita exigem CSRF. Tentar marcar uma notificação de outro destinatário retorna 404, sem revelar sua existência. A API retorna DTOs, sem entidades, IDs internos ou dados de contato do ator.

Cada item inclui `notificationId`, `type`, `title`, `message`, `actor` (UUID, nome, username e foto), `postId`, `postAvailable`, `createdAt`, `readAt` e `read`. Se a publicação tiver sido excluída, o aviso é preservado como histórico e `postAvailable` fica falso.

## Front-end

O sino compartilhado aparece no feed, no perfil próprio, no perfil de outros usuários e na página de publicação. Ele abre `/notifications`, uma rota autenticada com paginação, estados de carregamento/erro/vazio, acesso ao perfil do ator e à publicação relacionada.

A página permite marcar um aviso ou todos como lidos. Abrir uma publicação por um aviso não lido primeiro confirma a leitura na API. Posts removidos mantêm o histórico sem oferecer navegação para a publicação indisponível. As escritas usam CSRF e atualizam o estado da interface sem recarregar o documento.

O contador é consultado ao montar o sino, a cada 30 segundos com a aba visível, ao voltar à aba e depois das operações de leitura. A lista é carregada ao abrir a página e pode ser atualizada pelo botão Atualizar. Não há WebSocket nem entrega de e-mail nesta etapa.

No ambiente dev, reiniciar a API cria a tabela via Hibernate. Ambientes com migrações controladas devem criar a tabela, índices e chaves estrangeiras antes da ativação.

## Amizades

Solicitações e aceites também usam `NotificationService` no canal do site. O destinatário do pedido recebe `FRIEND_REQUEST_RECEIVED`; o solicitante recebe `FRIEND_REQUEST_ACCEPTED` quando a amizade é aceita. Esses avisos têm ator e `postId` nulo. A interface direciona o usuário para as solicitações recebidas ou para a lista de amigos. Regras e rotas estão em `friendships.md`.
