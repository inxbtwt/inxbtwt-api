package com.inxbtwt.domain.service.search;

import com.inxbtwt.domain.dto.search.SearchUserResponse;
import com.inxbtwt.infra.client.search.SearchApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchApiClient searchApiClient;

    public Page<SearchUserResponse> searchUsers(String user, int page, int size) {
        var response = searchApiClient.searchUsers(user.trim(), page, size);
        if (response == null || response.data() == null || response.data().content() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        }

        var result = response.data();
        if (result.number() != page || result.size() != size || result.totalElements() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        }

        return new PageImpl<>(result.content(), PageRequest.of(page, size), result.totalElements());
    }
}
