package com.inxbtwt.domain.service.user;

import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.db.repository.UserDataRepository;
import com.inxbtwt.domain.dto.user.http.PatchUserRequest;
import com.inxbtwt.domain.dto.user.http.UpdateUserRequest;
import com.inxbtwt.domain.mapper.user.UserMapper;
import com.inxbtwt.infra.exception.domain.user.UserAlreadyExistException;
import com.inxbtwt.infra.exception.domain.user.UserInvalidException;
import com.inxbtwt.infra.exception.domain.user.UserNotFoundException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserUpdateServiceTest {
    private final UserDataRepository repository = mock(UserDataRepository.class);
    private final UUID id = UUID.randomUUID();
    private final UserDataEntity existing = UserDataEntity.builder().id(1).userId(id)
            .name("Maria Silva").photoUrl("http://localhost/photo.png")
            .email("maria@example.com").phone("11999999999").password("valid-password").build();

    @Test
    void patchPreservesOmittedFieldsAndIdentity() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new UserServiceImpl(repository, new UserMapper(), factory.getValidator());
            when(repository.findByUserId(id)).thenReturn(Optional.of(existing));
            when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
            var result = service.patchUser(id, PatchUserRequest.builder().name("Novo Nome").build());
            assertEquals("Novo Nome", result.name());
            assertEquals(existing.getEmail(), result.email());
            assertEquals(id, result.userId());
            var saved = org.mockito.ArgumentCaptor.forClass(UserDataEntity.class);
            verify(repository).saveAndFlush(saved.capture());
            assertEquals(existing.getId(), saved.getValue().getId());
            assertEquals(existing.getPassword(), saved.getValue().getPassword());
        }
    }

    @Test
    void putReplacesEditableFields() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new UserServiceImpl(repository, new UserMapper(), factory.getValidator());
            when(repository.findByUserId(id)).thenReturn(Optional.of(existing));
            when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
            var result = service.updateUser(id, new UpdateUserRequest("Novo Nome", "http://localhost/new.png",
                    "novo@example.com", "11888888888", "new-password"));
            assertEquals("novo@example.com", result.email());
            assertEquals("11888888888", result.phone());
            assertEquals(id, result.userId());
        }
    }

    @Test
    void rejectsEmptyOrInvalidPatchBeforeSaving() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new UserServiceImpl(repository, new UserMapper(), factory.getValidator());
            when(repository.findByUserId(id)).thenReturn(Optional.of(existing));
            assertThrows(UserInvalidException.class, () -> service.patchUser(id, PatchUserRequest.builder().build()));
            assertThrows(UserInvalidException.class, () -> service.patchUser(id, PatchUserRequest.builder().name(" ").build()));
            assertThrows(UserInvalidException.class, () -> service.patchUser(id, PatchUserRequest.builder().email("invalid").build()));
            verify(repository, never()).saveAndFlush(any());
        }
    }

    @Test
    void rejectsEmailOwnedByAnotherUser() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new UserServiceImpl(repository, new UserMapper(), factory.getValidator());
            when(repository.findByUserId(id)).thenReturn(Optional.of(existing));
            when(repository.existsByEmailIgnoreCaseAndUserIdNot("other@example.com", id)).thenReturn(true);
            assertThrows(UserAlreadyExistException.class, () -> service.patchUser(id,
                    PatchUserRequest.builder().email("other@example.com").build()));
            verify(repository, never()).saveAndFlush(any());
        }
    }

    @Test
    void rejectsMissingUserForReadPutAndPatch() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new UserServiceImpl(repository, new UserMapper(), factory.getValidator());
            when(repository.findByUserId(id)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () -> service.getUser(id));
            assertThrows(UserNotFoundException.class, () -> service.updateUser(id, null));
            assertThrows(UserNotFoundException.class, () -> service.patchUser(id, null));
        }
    }
}
