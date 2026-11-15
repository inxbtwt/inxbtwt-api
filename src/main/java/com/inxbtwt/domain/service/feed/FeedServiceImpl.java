package com.inxbtwt.domain.service.feed;

import com.inxbtwt.domain.dto.feed.http.FeedResponse;
import com.inxbtwt.domain.util.UserAuthUtil;
import com.inxbtwt.infra.client.auth.ServiceAccessTokenProvider;
import com.inxbtwt.infra.client.feed.FeedApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final UserAuthUtil userAuthUtil;
    private final FeedApiClient feedApiClient;
    private final ServiceAccessTokenProvider serviceAccessTokenProvider;

    @Override
    public FeedResponse getFeed(String strategy, int page, int size) {
        var userId = userAuthUtil.getUserId();
        var response = feedApiClient.getFeed(
                "Bearer " + serviceAccessTokenProvider.accessToken("feed-api"),
                userId, strategy, page, size);
        if (response == null || response.data() == null || response.data().content() == null) {
            log.error("Resposta vazia ou inválida da feed-api");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        }

        var result = response.data();
        if (result.page() != page || result.size() != size || result.content().size() > size
                || result.strategy() == null || result.strategy().isBlank()) {
            log.error("Resposta inconsistente da feed-api");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        }

        return result;
    }
}
