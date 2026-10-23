package com.inxbtwt.domain.util;

import com.inxbtwt.domain.dto.user.http.PatchUserRequest;
import com.inxbtwt.infra.exception.domain.user.UserAlreadyExistException;
import com.inxbtwt.infra.exception.domain.user.UserInvalidException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.UUID;
import java.util.stream.Collectors;

public final class UserUtils {

    private UserUtils() {
    }

    public static <T> void validateFields(T request, Validator validator) {
        if (request == null) {
            throw new UserInvalidException("Os dados do usuário são obrigatórios.");
        }
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(" "));
            throw new UserInvalidException(message);
        }
    }

    public static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new UserInvalidException("O identificador do usuário é obrigatório.");
        }
    }

    public static void validatePatch(PatchUserRequest request) {
        if (request == null || (request.name() == null && request.photoUrl() == null
                && request.email() == null && request.phone() == null && request.password() == null)) {
            throw new UserInvalidException("Informe pelo menos um campo para atualizar o usuário.");
        }
    }

    public static void validateEmailAvailability(boolean emailAlreadyExists) {
        if (emailAlreadyExists) {
            throw new UserAlreadyExistException("Já existe um usuário cadastrado com este e-mail.");
        }
    }
}
