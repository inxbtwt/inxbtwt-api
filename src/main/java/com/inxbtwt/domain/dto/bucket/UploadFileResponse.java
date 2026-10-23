package com.inxbtwt.domain.dto.bucket;

import java.io.Serializable;
import lombok.Builder;

@Builder
public record UploadFileResponse(
        String bucket,
        String key,
        String url,
        String fileName,
        String contentType,
        long size
) implements Serializable {
}
