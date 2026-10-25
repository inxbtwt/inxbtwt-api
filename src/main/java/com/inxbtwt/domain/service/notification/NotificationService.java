package com.inxbtwt.domain.service.notification;

import com.inxbtwt.domain.dto.notification.NotificationCommand;
import com.inxbtwt.domain.enums.NotificationChannel;

/** Ponto de entrada para selecionar o canal sem acoplar o chamador à persistência ou ao provedor de e-mail. */
public interface NotificationService {

    void send(NotificationCommand command, NotificationChannel channel);

    default void send(NotificationCommand command) {
        send(command, NotificationChannel.IN_APP);
    }
}
