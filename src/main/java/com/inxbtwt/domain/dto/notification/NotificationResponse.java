package com.inxbtwt.domain.dto.notification;

import com.inxbtwt.domain.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        NotificationType type,
        String title,
        String message,
        NotificationActorResponse actor,
        UUID postId,
        boolean postAvailable,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        boolean read
) {
}
