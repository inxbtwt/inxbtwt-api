package com.inxbtwt.domain.dto.notification;

import java.util.UUID;

public record NotificationActorResponse(UUID userId, String name, String username, String photoUrl) {
}
