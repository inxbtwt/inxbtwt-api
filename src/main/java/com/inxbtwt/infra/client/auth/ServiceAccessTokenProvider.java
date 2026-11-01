package com.inxbtwt.infra.client.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceAccessTokenProvider {

    private static final String CLIENT_REGISTRATION_ID = "search-api";
    private static final String PRINCIPAL_NAME = "inxbtwt-api";

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public String accessToken() {
        var request = OAuth2AuthorizeRequest.withClientRegistrationId(CLIENT_REGISTRATION_ID)
                .principal(PRINCIPAL_NAME)
                .build();
        try {
            var authorizedClient = authorizedClientManager.authorize(request);
            if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
                log.error("Auth-api não devolveu credencial de serviço para a search-api");
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Não foi possível obter credencial para a API de busca.");
            }

            return authorizedClient.getAccessToken().getTokenValue();
        } catch (OAuth2AuthorizationException exception) {
            log.error("Falha ao obter credencial de serviço no auth-api: {}", exception.getError().getErrorCode());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível obter credencial para a API de busca.", exception);
        }
    }
}
