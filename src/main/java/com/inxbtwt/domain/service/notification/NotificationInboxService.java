package com.inxbtwt.domain.service.notification;

import com.inxbtwt.domain.dto.notification.NotificationPageResponse;
import com.inxbtwt.domain.dto.notification.NotificationResponse;

import java.util.UUID;

/** Opera apenas a caixa do site do usuário autenticado; não controla entrega de e-mail. */
public interface NotificationInboxService {

    NotificationPageResponse getNotifications(int page, int size);

    long countUnread();

    NotificationResponse markAsRead(UUID notificationId);

    void markAllAsRead();
}
