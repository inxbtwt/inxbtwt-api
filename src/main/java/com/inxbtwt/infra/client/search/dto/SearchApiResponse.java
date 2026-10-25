package com.inxbtwt.infra.client.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inxbtwt.domain.dto.search.SearchUserResponse;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchApiResponse(SearchPage data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchPage(
            List<SearchUserResponse> content,
            long totalElements,
            int number,
            int size
    ) {
    }
}
