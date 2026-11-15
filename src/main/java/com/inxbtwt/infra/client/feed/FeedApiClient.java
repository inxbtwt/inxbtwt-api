package com.inxbtwt.infra.client.feed;

import com.inxbtwt.infra.client.feed.dto.FeedApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "feed-api", url = "${app.feed-api.url}")
public interface FeedApiClient {

    @GetMapping("/api/feed")
    FeedApiResponse getFeed(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("userId") UUID userId,
            @RequestParam(value = "strategy", required = false) String strategy,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}
