package com.inxbtwt.infra.client.feed.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inxbtwt.domain.dto.feed.http.FeedResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FeedApiResponse(FeedResponse data) {
}
