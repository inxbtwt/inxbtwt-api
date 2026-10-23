package com.inxbtwt.infra.config.s3;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.s3")
public record S3Properties(
        URI endpoint,
        String region,
        String accessKey,
        String secretKey,
        String bucket) {
}
