package com.inxbtwt.domain.dto.post.http;

import java.util.UUID;

public record PostAuthorResponse(UUID userId, String name, String username, String photoUrl) {
}
