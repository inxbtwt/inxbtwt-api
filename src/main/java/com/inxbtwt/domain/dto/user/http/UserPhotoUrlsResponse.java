package com.inxbtwt.domain.dto.user.http;

import java.util.UUID;

public record UserPhotoUrlsResponse(UUID userId, String profileUrl, String coverUrl) {
}
