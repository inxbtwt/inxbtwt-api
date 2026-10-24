package com.inxbtwt.domain.dto.jwt;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Informe o e-mail ou username.")
        @JsonAlias("email")
        String identifier,

        @NotBlank(message = "A senha é obrigatória.")
        String password
) {
}
