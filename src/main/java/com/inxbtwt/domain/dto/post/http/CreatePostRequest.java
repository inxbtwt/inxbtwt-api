package com.inxbtwt.domain.dto.post.http;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank(message = "O conteúdo da publicação é obrigatório.")
        String content
) {
}
