package com.inxbtwt.infra.client.search;

import com.inxbtwt.infra.client.search.dto.SearchApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "search-api", url = "${app.search-api.url}")
public interface SearchApiClient {

    @GetMapping("/api/search")
    SearchApiResponse searchUsers(
            @RequestParam("user") String user,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}
