package com.inxbtwt.domain.service.user;

import com.inxbtwt.db.repository.UserDataRepository;
import com.inxbtwt.domain.dto.user.http.CreateUserRequest;
import com.inxbtwt.domain.mapper.user.UserMapper;
import com.inxbtwt.infra.exception.domain.user.UserAlreadyExistException;
import com.inxbtwt.infra.exception.domain.user.UserInvalidException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private final UserDataRepository repository = mock(UserDataRepository.class);
    private final UserMapper mapper = mock(UserMapper.class);

    @Test
    void rejectsNullRequestBeforeMappingOrQueryingDatabase() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new UserServiceImpl(repository, mapper, factory.getValidator());
            var exception = assertThrows(UserInvalidException.class, () -> service.createUser(null));
            assertEquals("Os dados do usuário são obrigatórios.", exception.getMessage());
            verifyNoInteractions(repository, mapper);
        }
    }

    @Test
    void rejectsInvalidFieldsWithoutIncludingPasswordInMessage() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new UserServiceImpl(repository, mapper, factory.getValidator());
            var request = new CreateUserRequest("", "invalid-url", "invalid-email", "", "secret");
            var exception = assertThrows(UserInvalidException.class, () -> service.createUser(request));
            assertTrue(exception.getMessage().contains("O nome é obrigatório."));
            assertTrue(exception.getMessage().contains("O e-mail deve ser válido."));
            assertTrue(exception.getMessage().contains("A URL da foto deve ser válida."));
            assertTrue(exception.getMessage().contains("O telefone é obrigatório."));
            assertTrue(exception.getMessage().contains("A senha deve ter entre 8 e 255 caracteres."));
            assertFalse(exception.getMessage().contains(request.password()));
            verifyNoInteractions(repository, mapper);
        }
    }

    @Test
    void rejectsEmailAlreadyRegisteredBeforeMapping() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new UserServiceImpl(repository, mapper, factory.getValidator());
            var request = validRequest();
            when(repository.existsByEmailIgnoreCase(request.email())).thenReturn(true);
            assertThrows(UserAlreadyExistException.class, () -> service.createUser(request));
            verify(repository).existsByEmailIgnoreCase(request.email());
            verifyNoInteractions(mapper);
        }
    }

    @Test
    void allowsValidRequestWithNewEmailToReachMapper() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new UserServiceImpl(repository, mapper, factory.getValidator());
            var request = validRequest();
            assertDoesNotThrow(() -> service.createUser(request));
            verify(repository).existsByEmailIgnoreCase(request.email());
            verify(mapper).toEntity(request);
        }
    }

    private CreateUserRequest validRequest() {
        return new CreateUserRequest("Maria", "http://localhost:8333/avatar.png",
                "maria@example.com", "11999999999", "valid-password");
    }
}
