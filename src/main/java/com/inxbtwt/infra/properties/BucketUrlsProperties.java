package com.inxbtwt.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "app.bucket-urls")
public record BucketUrlsProperties(
        String photoProfile,
        String coverProfile
) {
}