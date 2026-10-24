package com.inxbtwt.domain.dto.profile.http;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record PatchProfileRequest(
        String bio,
        @Size(max = 255, message = "A URL da capa deve ter no máximo 255 caracteres.")
        @URL(message = "Informe uma URL válida para a capa.")
        String coverUrl
) {
}
