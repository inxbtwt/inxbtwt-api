package com.inxbtwt.domain.dto.profile.http;

import java.util.UUID;

public record ProfileResponse(Long profileId, UUID userId, String bio, String coverUrl) {
}
