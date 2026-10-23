package com.inxbtwt.domain.dto.user.http;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record UpdateUserRequest(
    @NotBlank(message = "O nome é obrigatório.")
    @Size(min = 3, max = 30, message = "O nome deve ter entre 3 e 30 caracteres.")
    String name,

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail deve ser válido.")
    @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
    String email,

    @NotBlank(message = "O telefone é obrigatório.")
    @Size(max = 255, message = "O telefone deve ter no máximo 255 caracteres.")
    String phone,

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 8, max = 255, message = "A senha deve ter entre 8 e 255 caracteres.")
    String password
) implements Serializable {
}
