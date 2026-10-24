package com.inxbtwt.domain.dto.notification;

import java.util.List;

public record NotificationPageResponse(
        List<NotificationResponse> content,
        int page,
        int size,
        boolean hasNext,
        boolean hasPrevious
) {
}
