package com.inxbtwt.infra.controller.search;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.search.SearchUserResponse;
import com.inxbtwt.domain.service.search.SearchService;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SearchUserResponse>>> searchUsers(
            @RequestParam
            @NotBlank(message = "Informe um username para pesquisar.")
            @Size(max = 255, message = "O termo de busca deve ter no máximo 255 caracteres.")
            String user,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "A página deve ser maior ou igual a zero.")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "O tamanho da página deve ser de 1 a 100.")
            @Max(value = 100, message = "O tamanho da página deve ser de 1 a 100.")
            int size
    ) {
        return ApiControllerUtils.ok("Usuários encontrados com sucesso.",
                searchService.searchUsers(user, page, size));
    }
}
