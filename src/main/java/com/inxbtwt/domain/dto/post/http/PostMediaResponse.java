package com.inxbtwt.domain.dto.post.http;

import java.util.UUID;

public record PostMediaResponse(
        UUID mediaId,
        String url,
        String contentType,
        long size,
        int position,
        String provider,
        String externalId
) {
}
