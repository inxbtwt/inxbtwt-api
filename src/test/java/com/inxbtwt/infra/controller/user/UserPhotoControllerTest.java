package com.inxbtwt.infra.controller.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.inxbtwt.domain.dto.bucket.UploadFileResponse;
import com.inxbtwt.domain.dto.user.http.UserResponse;
import com.inxbtwt.domain.service.user.UserService;
import com.inxbtwt.infra.exception.bucket.BucketStorageException;
import com.inxbtwt.infra.exception.http.UserExceptionHandler;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserPhotoControllerTest {
    @Test
    void urlsEndpointReturnsBothTypes() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getPhotoUrls(id)).thenReturn(new com.inxbtwt.domain.dto.user.http.UserPhotoUrlsResponse(
                id, "http://localhost/profile", "http://localhost/cover"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/users/{userId}/photos", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.profileUrl").value("http://localhost/profile"))
                .andExpect(jsonPath("$.data.coverUrl").value("http://localhost/cover"));
    }

    @Test
    void downloadReturnsBinaryImageAndAcceptsEncodedUrl() throws Exception {
        UUID id = UUID.randomUUID();
        String url = "http://localhost:8333/bucket/img/profile/photo/example";
        byte[] data = {1, 2, 3};
        when(service.downloadPhoto(id, url)).thenReturn(new com.inxbtwt.domain.dto.bucket.DownloadFileResponse(
                "key", data, "image/png", data.length));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/users/{userId}/photo", id)
                        .param("url", url))
                .andExpect(status().isOk()).andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(data)).andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void coverUploadUsesSelectedTypeAndRejectsInvalidType() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(multipart("/users/{userId}/photo", id).file(photo).param("type", "COVER"))
                .andExpect(status().isOk());
        verify(service).uploadPhoto(eq(id), any(), eq(com.inxbtwt.domain.enums.UserPhotoType.COVER));
        clearInvocations(service);
        mvc.perform(multipart("/users/{userId}/photo", id).file(photo).param("type", "INVALID"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    private final UserService service = mock(UserService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new UserController(service))
            .setControllerAdvice(new UserExceptionHandler()).build();
    private final MockMultipartFile photo = new MockMultipartFile("photo", "avatar.png", "image/png", new byte[]{1});

    @Test
    void uploadReturnsFileUrlInApiResponse() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.uploadPhoto(eq(id), any(), eq(com.inxbtwt.domain.enums.UserPhotoType.PROFILE))).thenReturn(new UploadFileResponse("bucket", "key",
                "http://localhost:8333/bucket/key", "avatar.png", "image/png", 1));
        mvc.perform(multipart("/users/{userId}/photo", id).file(photo))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.url").value("http://localhost:8333/bucket/key"))
                .andExpect(jsonPath("$.message").value("Foto do usuário atualizada com sucesso."));
    }

    @Test
    void creationAcceptsJsonBodyWithoutPhoto() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.createUser(any())).thenReturn(UserResponse.builder().userId(id).build());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/users")
                        .contentType("application/json").content("""
                        {"name":"Lucas","email":"lucas@example.com","phone":"11999999999","password":"password123"}
                        """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.userId").value(id.toString()));
        verify(service).createUser(argThat(request -> request.email().equals("lucas@example.com")));
    }

    @Test
    void invalidJsonUserIsRejectedBeforeService() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/users")
                        .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value("ERROR"));
        verifyNoInteractions(service);
    }

    @Test
    void missingPhotoReturnsBadRequestWithoutCallingService() throws Exception {
        mvc.perform(multipart("/users/{userId}/photo", UUID.randomUUID()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value("ERROR"));
        verifyNoInteractions(service);
    }

    @Test
    void bucketFailureReturnsStandardErrorWithoutExposingDetails() throws Exception {
        when(service.uploadPhoto(any(), any(), any())).thenThrow(new BucketStorageException("private storage details", null));
        mvc.perform(multipart("/users/{userId}/photo", UUID.randomUUID()).file(photo))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private storage details"))));
    }
}
