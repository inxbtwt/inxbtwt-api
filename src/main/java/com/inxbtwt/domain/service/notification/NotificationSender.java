package com.inxbtwt.domain.service.notification;

import com.inxbtwt.domain.dto.notification.NotificationCommand;
import com.inxbtwt.domain.enums.NotificationChannel;

/**
 * Estratégia de entrega de um canal.
 *
 * <p>Registre uma implementação como bean Spring e retorne o canal em {@link #channel()}.
 * O serviço descobre os canais automaticamente; não é necessário adicionar condicionais
 * ao despachante. O canal do site persiste na caixa de entrada. Uma implementação EMAIL
 * deverá resolver o destinatário pelo UUID e delegar o envio a um provedor configurado,
 * sem criar um registro lido/não lido na caixa do site por consequência do envio.
 *
 * <p>A implementação deve propagar falhas de entrega. Não trate indisponibilidade do
 * provedor como sucesso. Para enviar uma mesma ação em dois canais, cada entrega precisa
 * de seu próprio controle de falhas e retentativas; essa coordenação não é uma transação SMTP/JPA.
 */
public interface NotificationSender {

    NotificationChannel channel();

    void send(NotificationCommand command);
}
