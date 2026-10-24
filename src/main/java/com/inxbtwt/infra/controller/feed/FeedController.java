package com.inxbtwt.infra.controller.feed;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.feed.http.FeedResponse;
import com.inxbtwt.domain.service.feed.FeedService;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import com.inxbtwt.infra.exception.http.HttpMessages;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<ApiResponse<FeedResponse>> getFeed(
            @RequestParam(required = false) String strategy,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiControllerUtils.ok(HttpMessages.FEED_FOUND, feedService.getFeed(strategy, page, size));
    }
}
