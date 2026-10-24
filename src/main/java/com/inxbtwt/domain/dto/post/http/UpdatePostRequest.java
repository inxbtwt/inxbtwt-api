package com.inxbtwt.domain.dto.post.http;

import jakarta.validation.constraints.NotNull;

public record UpdatePostRequest(
        @NotNull(message = "Informe o texto da publicação.")
        String content
) {
}
