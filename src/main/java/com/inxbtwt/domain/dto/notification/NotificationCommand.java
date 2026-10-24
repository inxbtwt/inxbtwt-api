package com.inxbtwt.domain.dto.notification;

import com.inxbtwt.domain.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Comando interno compartilhado pelos canais; não recebe endereço de e-mail nem HTML arbitrário. */
public record NotificationCommand(
        @NotNull(message = "O destinatário da notificação é obrigatório.")
        UUID recipientId,
        UUID actorId,
        UUID postId,
        @NotNull(message = "O tipo da notificação é obrigatório.")
        NotificationType type,
        @NotBlank(message = "O título da notificação é obrigatório.")
        @Size(max = 160, message = "O título deve ter no máximo 160 caracteres.")
        String title,
        @NotBlank(message = "A mensagem da notificação é obrigatória.")
        @Size(max = 2000, message = "A mensagem deve ter no máximo 2000 caracteres.")
        String message
) {
}
