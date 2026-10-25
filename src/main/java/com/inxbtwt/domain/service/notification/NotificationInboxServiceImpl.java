package com.inxbtwt.domain.service.notification;

import com.inxbtwt.db.entity.NotificationEntity;
import com.inxbtwt.db.repository.NotificationRepository;
import com.inxbtwt.domain.dto.notification.NotificationActorResponse;
import com.inxbtwt.domain.dto.notification.NotificationPageResponse;
import com.inxbtwt.domain.dto.notification.NotificationResponse;
import com.inxbtwt.domain.util.UserAuthUtil;
import com.inxbtwt.infra.exception.domain.DomainException;
import com.inxbtwt.infra.exception.http.NotFoundHttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationInboxServiceImpl implements NotificationInboxService {

    private final NotificationRepository notifications;
    private final UserAuthUtil auth;

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(int page, int size) {
        var recipientId = auth.getUserId();
        if (page < 0 || size < 1 || size > 100) {
            throw new DomainException("Informe página não negativa e tamanho entre 1 e 100.");
        }
        var slice = notifications.findByRecipient_UserIdOrderByCreatedAtDescIdDesc(
                recipientId, PageRequest.of(page, size));
        return new NotificationPageResponse(slice.getContent().stream().map(this::toResponse).toList(),
                slice.getNumber(), slice.getSize(), slice.hasNext(), slice.hasPrevious());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread() {
        return notifications.countByRecipient_UserIdAndReadAtIsNull(auth.getUserId());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        var recipientId = auth.getUserId();
        notifications.markAsRead(notificationId, recipientId, LocalDateTime.now());
        var notification = notifications.findByNotificationIdAndRecipient_UserId(notificationId, recipientId)
                .orElseThrow(() -> new NotFoundHttpException("Notificação não encontrada."));
        return toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        notifications.markAllAsRead(auth.getUserId(), LocalDateTime.now());
    }

    private NotificationResponse toResponse(NotificationEntity notification) {
        var actor = notification.getActor();
        var actorResponse = actor == null ? null : new NotificationActorResponse(
                actor.getUserId(), actor.getName(), actor.getUsername(), actor.getPhotoUrl());
        var post = notification.getPost();
        return new NotificationResponse(notification.getNotificationId(), notification.getType(),
                notification.getTitle(), notification.getMessage(), actorResponse,
                post == null ? null : post.getPostId(), post != null && post.getDeletedAt() == null,
                notification.getCreatedAt(), notification.getReadAt(), notification.isRead());
    }
}
