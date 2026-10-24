# Amizades e solicitações

A amizade é bilateral: enviar uma solicitação não torna os usuários amigos. Apenas o destinatário pode aceitar ou recusar; apenas o remetente pode cancelar. Qualquer um dos amigos pode desfazer a amizade. Não é permitido criar relação consigo mesmo.

## Persistência e concorrência

`FriendshipEntity` guarda um par de usuários ordenado pelos IDs internos (`first_user_id < second_user_id`), solicitante, estado (`NONE`, `PENDING`, `ACCEPTED`) e datas. A restrição única do par impede duas linhas para a mesma relação, inclusive com pedidos em sentidos opostos.

`FriendshipService` é o contrato da camada. A implementação insere a linha neutra com `ON CONFLICT DO NOTHING` e bloqueia a relação com `PESSIMISTIC_WRITE` antes de mudar seu estado. Pedidos cruzados não são aceitos automaticamente: o segundo usuário recebe o estado relativo `REQUEST_RECEIVED` e precisa aceitar explicitamente.

Enviar novamente uma solicitação pendente ou repetir um aceite concluído retorna o estado atual sem nova notificação. Recusar/cancelar/desfazer retorna a relação para `NONE`, permitindo um novo pedido. Repetir a limpeza de uma relação já neutra é idempotente. Datas retornadas têm precisão de microssegundos, compatível com PostgreSQL.

As operações usam o usuário autenticado como uma das partes e o UUID público da outra pessoa como alvo. As listagens usam sempre o usuário da sessão. A API não expõe IDs internos ou contatos.

## Rotas

Todas exigem autenticação; escritas também exigem CSRF.

- `GET /friendships?page=0&size=20`: amigos do usuário autenticado, por data de aceite decrescente.
- `GET /friendships/requests?direction=RECEIVED&page=0&size=20`: solicitações recebidas. `direction=SENT` lista enviadas. Apenas pendentes são retornadas.
- `GET /friendships/count`: quantidade de amigos aceitos, como `{count}`.
- `GET /friendships/{userId}`: estado relativo à outra pessoa.
- `POST /friendships/{userId}/request`: enviar solicitação.
- `PATCH /friendships/{userId}/accept`: aceitar solicitação recebida.
- `PATCH /friendships/{userId}/decline`: recusar solicitação recebida.
- `DELETE /friendships/{userId}/cancel`: cancelar solicitação enviada.
- `DELETE /friendships/{userId}`: desfazer amizade aceita.

As escritas retornam o estado atualizado com `{user,state,requestedAt,acceptedAt}`. `user` contém UUID, nome, username e foto. Os estados públicos são `NONE`, `REQUEST_SENT`, `REQUEST_RECEIVED`, `FRIENDS` e `SELF`.

As listagens retornam `{content,page,size,hasNext,hasPrevious}`. A página inicia em zero e o tamanho permitido é de 1 a 100. Usuário inexistente retorna 404, ação reservada à outra parte retorna 403 e transição incompatível com o estado atual retorna 409.

## Notificações

O destinatário recebe `FRIEND_REQUEST_RECEIVED` quando um pedido é criado. O remetente recebe `FRIEND_REQUEST_ACCEPTED` quando o destinatário aceita. Ambos usam o canal do site e participam da mesma transação da mudança de estado. Pedidos duplicados não duplicam avisos. Recusa, cancelamento e desfazer amizade não notificam; avisos anteriores são mantidos como histórico.

Esses avisos não estão associados a posts. A tela de notificações leva à aba Recebidas ou à lista de amigos.

## Front-end

O perfil de outro usuário mostra Adicionar amigo, solicitação enviada com opção de cancelar, solicitação recebida com aceitar/recusar ou Amigos com confirmação para desfazer.

`/friends` oferece abas Amigos, Recebidas e Enviadas, contagem de amigos, paginação, links para os perfis e ações locais sem recarregar o documento. As linhas reutilizam os dados da listagem, evitando uma consulta de estado por pessoa. Os menus desktop e mobile dão acesso à página. Novas solicitações recebidas podem ser consultadas com Atualizar ou pelo atalho na notificação.

## Feed

A amizade não é usada para filtrar ou ordenar o feed. A estratégia cronológica global e a abstração de estratégias permanecem com o mesmo comportamento. Filtros por amigos ou outras recomendações podem ser implementados depois, consultando as relações aceitas.

Reinicie a API no ambiente dev para que o Hibernate crie a tabela. Ambientes com migrações controladas devem criar tabela, índices, chaves estrangeiras e restrições antes de ativar as rotas.

### Notificações em bancos já existentes

A inicialização do perfil dev aplica `db/notification-types-schema.sql`, atualizando a restrição `notification_entity_type_check` para aceitar também os dois eventos de amizade. O `ddl-auto: update` não atualiza essa restrição antiga automaticamente. Em ambientes com migrações controladas, inclua a mesma mudança. Ao adicionar novos tipos de notificação, atualize também essa lista permitida.

## Contagem nos perfis

As respostas `GET /profiles/me`, `PATCH /profiles/me` e `GET /profiles/username/{username}` incluem `friendCount`, calculado apenas sobre relações `ACCEPTED`. O contador também é retornado quando o usuário ainda não possui uma linha de perfil. Solicitações pendentes não entram na quantidade.

O perfil próprio apresenta um link para a lista de amigos. O perfil visitado mostra a contagem do usuário consultado e atualiza apenas esse dado após ações de amizade, sem remontar o perfil ou recarregar suas publicações.
