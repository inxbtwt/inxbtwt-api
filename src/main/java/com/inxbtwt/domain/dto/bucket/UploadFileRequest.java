package com.inxbtwt.domain.dto.bucket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record UploadFileRequest(
        @NotNull(message = "O arquivo é obrigatório.")
        MultipartFile file,
        @NotBlank(message = "O caminho de destino no bucket é obrigatório.")
        String path
) {
}
