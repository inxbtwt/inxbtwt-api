package com.inxbtwt.domain.dto.profile.http;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProfileViewResponse(
        UUID userId,
        String name,
        String username,
        String bio,
        String photoUrl,
        String coverUrl,
        LocalDateTime createdAt,
        long friendCount
) {
}
