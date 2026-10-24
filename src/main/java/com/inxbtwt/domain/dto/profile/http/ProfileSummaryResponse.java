package com.inxbtwt.domain.dto.profile.http;

import java.util.UUID;

public record ProfileSummaryResponse(UUID userId, String name, String username, String photoUrl) {
}
