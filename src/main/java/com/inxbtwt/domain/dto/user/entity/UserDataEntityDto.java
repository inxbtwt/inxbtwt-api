package com.inxbtwt.domain.dto.user.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
public record UserDataEntityDto(
    @Size(min = 3, max = 30, message = "O nome deve ter entre 3 e 30 caracteres.")
    @NotBlank(message = "O nome é obrigatório.")
    String name,

    @NotBlank(message = "A URL da foto é obrigatória.")
    @Size(max = 255, message = "A URL da foto deve ter no máximo 255 caracteres.")
    @URL(message = "A URL da foto deve ser válida.")
    String photoUrl,

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail deve ser válido.")
    @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
    String email,

    @NotBlank(message = "O telefone é obrigatório.")
    String phone,

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 8, max = 255, message = "A senha deve ter entre 8 e 255 caracteres.")
    String password,

    @PastOrPresent(message = "A data de atualização não pode estar no futuro.")
    LocalDateTime updatedAt
) implements Serializable {
}
