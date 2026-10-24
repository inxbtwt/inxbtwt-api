package com.inxbtwt.domain.dto.feed.http;

import com.inxbtwt.domain.dto.post.http.PostResponse;

import java.util.List;

public record FeedResponse(
        String strategy,
        List<PostResponse> content,
        int page,
        int size,
        boolean hasNext,
        boolean hasPrevious
) {
}
