package com.inxbtwt.domain.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.db.repository.UserDataRepository;
import com.inxbtwt.domain.dto.bucket.UploadFileRequest;
import com.inxbtwt.domain.dto.bucket.UploadFileResponse;
import com.inxbtwt.domain.dto.user.http.CreateUserRequest;
import com.inxbtwt.domain.mapper.user.UserMapper;
import com.inxbtwt.domain.service.bucket.BucketService;
import com.inxbtwt.infra.exception.bucket.BucketStorageException;
import com.inxbtwt.infra.exception.domain.user.UserAlreadyExistException;
import com.inxbtwt.infra.exception.domain.user.UserNotFoundException;
import com.inxbtwt.infra.properties.BucketUrlsProperties;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

class UserPhotoServiceTest {
    @Test
    void urlsIncludeProfileAndCoverAndAllowMissingCover() {
        UUID id = UUID.randomUUID();
        var user = UserDataEntity.builder().userId(id).photoUrl("profile").coverUrl("cover").build();
        when(repository.findByUserId(id)).thenReturn(Optional.of(user));
        var response = service.getPhotoUrls(id);
        assertThat(response.userId()).isEqualTo(id);
        assertThat(response.profileUrl()).isEqualTo("profile");
        assertThat(response.coverUrl()).isEqualTo("cover");
        when(repository.findByUserId(id)).thenReturn(Optional.of(user.toBuilder().coverUrl(null).build()));
        assertThat(service.getPhotoUrls(id).coverUrl()).isNull();
        verifyNoInteractions(bucket);
    }

    @Test
    void coverUploadPreservesProfileAndUsesCoverPath() {
        UUID id = UUID.randomUUID();
        var user = UserDataEntity.builder().userId(id).photoUrl("profile").build();
        when(repository.findByUserId(id)).thenReturn(Optional.of(user));
        when(bucket.upload(any())).thenReturn(uploaded);
        service.uploadPhoto(id, photo, com.inxbtwt.domain.enums.UserPhotoType.COVER);
        verify(bucket).upload(new UploadFileRequest(photo, "img/profile/cover/photo/"));
        var saved = ArgumentCaptor.forClass(UserDataEntity.class);
        verify(repository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getPhotoUrl()).isEqualTo("profile");
        assertThat(saved.getValue().getCoverUrl()).isEqualTo(uploaded.url());
    }

    @Test
    void downloadsOnlyUrlsCurrentlyAssignedToUser() {
        UUID id = UUID.randomUUID();
        when(repository.findByUserId(id)).thenReturn(Optional.of(UserDataEntity.builder()
                .userId(id).photoUrl("profile").coverUrl("cover").build()));
        service.downloadPhoto(id, "profile");
        service.downloadPhoto(id, "cover");
        verify(bucket).download("profile");
        verify(bucket).download("cover");
        assertThatThrownBy(() -> service.downloadPhoto(id, "another-user-photo"))
                .isInstanceOf(UserNotFoundException.class);
        verifyNoMoreInteractions(bucket);
    }

    private final UserDataRepository repository = mock(UserDataRepository.class);
    private final BucketService bucket = mock(BucketService.class);
    private final ValidatorFactory validation = Validation.buildDefaultValidatorFactory();
    private final UserServiceImpl service = new UserServiceImpl(repository, new UserMapper(),
            validation.getValidator(), bucket, new BucketUrlsProperties("img/profile/photo/", "img/profile/cover/photo/"));
    private final MockMultipartFile photo = new MockMultipartFile("photo", "avatar.png", "image/png", new byte[]{1});
    private final CreateUserRequest request = new CreateUserRequest("Lucas", "lucas@example.com", "11999999999", "password123");
    private final UploadFileResponse uploaded = new UploadFileResponse("bucket", "img/profile/photo/uuid",
            "http://localhost:8333/bucket/img/profile/photo/uuid", "avatar.png", "image/png", 1);

    @AfterEach
    void closeValidation() {
        validation.close();
    }

    @Test
    void creationWithoutPhotoDoesNotCallStorage() {
        when(repository.saveAndFlush(any())).thenAnswer(call -> {
            UserDataEntity user = call.getArgument(0);
            user.prePersist();
            return user;
        });

        var result = service.createUser(request);

        var saved = ArgumentCaptor.forClass(UserDataEntity.class);
        verify(repository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getPhotoUrl()).isNull();
        assertThat(saved.getValue().getCoverUrl()).isNull();
        assertThat(result.userId()).isNotNull();
        verifyNoInteractions(bucket);
    }

    @Test
    void replacementPreservesUserDataAndSavesNewUrl() {
        UUID id = UUID.randomUUID();
        var existing = UserDataEntity.builder().id(12).userId(id).name("Lucas")
                .email("lucas@example.com").password("password123").photoUrl("old-url").build();
        when(repository.findByUserId(id)).thenReturn(Optional.of(existing));
        when(bucket.upload(any())).thenReturn(uploaded);

        assertThat(service.uploadPhoto(id, photo, com.inxbtwt.domain.enums.UserPhotoType.PROFILE)).isEqualTo(uploaded);

        var saved = ArgumentCaptor.forClass(UserDataEntity.class);
        verify(repository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getPhotoUrl()).isEqualTo(uploaded.url());
        assertThat(saved.getValue().getUserId()).isEqualTo(id);
        assertThat(saved.getValue().getId()).isEqualTo(12);
        assertThat(saved.getValue().getEmail()).isEqualTo(existing.getEmail());
        assertThat(saved.getValue().getPassword()).isEqualTo(existing.getPassword());
    }

    @Test
    void nonexistentUserDoesNotUpload() {
        UUID id = UUID.randomUUID();
        when(repository.findByUserId(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.uploadPhoto(id, photo, com.inxbtwt.domain.enums.UserPhotoType.PROFILE)).isInstanceOf(UserNotFoundException.class);
        verifyNoInteractions(bucket);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void duplicateEmailDoesNotUpload() {
        when(repository.existsByEmailIgnoreCase(request.email())).thenReturn(true);
        assertThatThrownBy(() -> service.createUser(request)).isInstanceOf(UserAlreadyExistException.class);
        verifyNoInteractions(bucket);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void storageFailureDoesNotSaveUser() {
        when(bucket.upload(any())).thenThrow(new BucketStorageException("Falha no bucket.", null));
        UUID id = UUID.randomUUID();
        when(repository.findByUserId(id)).thenReturn(Optional.of(UserDataEntity.builder().userId(id).build()));
        assertThatThrownBy(() -> service.uploadPhoto(id, photo, com.inxbtwt.domain.enums.UserPhotoType.PROFILE)).isInstanceOf(BucketStorageException.class);
        verify(repository, never()).saveAndFlush(any());
    }
}
