package com.inxbtwt.domain.service.feed;

import com.inxbtwt.domain.dto.feed.http.FeedResponse;

public interface FeedService {

    FeedResponse getFeed(String strategy, int page, int size);
}
