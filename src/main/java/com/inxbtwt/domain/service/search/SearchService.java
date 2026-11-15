package com.inxbtwt.domain.service.search;

import com.inxbtwt.domain.dto.search.SearchUserResponse;
import com.inxbtwt.infra.client.search.SearchApiClient;
import com.inxbtwt.infra.client.auth.ServiceAccessTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchApiClient searchApiClient;
    private final ServiceAccessTokenProvider serviceAccessTokenProvider;

    public Page<SearchUserResponse> searchUsers(String user, int page, int size) {
        var response = searchApiClient.searchUsers(
                "Bearer " + serviceAccessTokenProvider.accessToken(),
                user.trim(),
                page,
                size
        );
        if (response == null || response.data() == null || response.data().content() == null) {
            log.error("Resposta vazia ou inválida da search-api");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        }

        var result = response.data();
        if (result.number() != page || result.size() != size || result.totalElements() < 0) {
            log.error("Paginação inconsistente da search-api: pediu página {} tamanho {}, recebeu página {} tamanho {}",
                    page, size, result.number(), result.size());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        }

        log.info("Busca de usuários concluída na search-api: {} resultado(s) na página, {} no total",
                result.content().size(), result.totalElements());

        return new PageImpl<>(result.content(), PageRequest.of(page, size), result.totalElements());
    }
}
