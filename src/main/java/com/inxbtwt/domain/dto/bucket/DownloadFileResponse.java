package com.inxbtwt.domain.dto.bucket;

import lombok.Builder;

@Builder
public record DownloadFileResponse(
        String key,
        byte[] data,
        String contentType,
        long size
) {
}
