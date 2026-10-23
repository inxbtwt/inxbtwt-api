package com.inxbtwt.infra.controller.user;

import com.inxbtwt.domain.dto.user.http.UserResponse;
import com.inxbtwt.domain.service.user.UserService;
import com.inxbtwt.infra.exception.domain.user.UserAlreadyExistException;
import com.inxbtwt.infra.exception.domain.user.UserInvalidException;
import com.inxbtwt.infra.exception.http.UserExceptionHandler;
import com.inxbtwt.infra.exception.http.NotFoundHttpException;
import com.inxbtwt.infra.exception.http.BadRequestHttpException;
import com.inxbtwt.infra.exception.http.InternalServerErrorHttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private final UserService service = mock(UserService.class);
    private MockMvc mvc;
    private static final String REQUEST = """
            {"name":"Maria","photoUrl":"http://localhost:8333/avatar.png",
             "email":"maria@example.com","phone":"11999999999","password":"valid-password"}
            """;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new UserController(service))
                .setControllerAdvice(new UserExceptionHandler()).build();
    }

    @Test
    void returnsCreatedUserWithoutPassword() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.createUser(any())).thenReturn(UserResponse.builder()
                .userId(id).name("Maria").email("maria@example.com").build());
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Usuário criado com sucesso."))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.userId").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("Maria"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
        verify(service).createUser(any());
    }

    @Test
    void rejectsInvalidBodyBeforeCallingService() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ERROR"));
        verifyNoInteractions(service);
    }

    @Test
    void returnsConflictForDuplicateUser() throws Exception {
        when(service.createUser(any())).thenThrow(new UserAlreadyExistException("E-mail já cadastrado."));
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado."))
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void returnsStandardResponseForHttpException() throws Exception {
        when(service.createUser(any())).thenThrow(new NotFoundHttpException());
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("O recurso solicitado não foi encontrado."))
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void rejectsMalformedJsonWithStandardResponse() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O corpo da requisição está ausente ou contém um JSON inválido."))
                .andExpect(jsonPath("$.status").value("ERROR"));
        verifyNoInteractions(service);
    }

    @Test
    void hidesInternalErrorDetails() throws Exception {
        when(service.createUser(any())).thenThrow(new IllegalStateException("detalhe interno"));
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno ao processar a requisição."))
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void providesFallbackForEmptyMessage() throws Exception {
        when(service.createUser(any())).thenThrow(new BadRequestHttpException(" "));
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Os dados da requisição são inválidos."));
    }

    @Test
    void hidesCustomInternalErrorMessage() throws Exception {
        when(service.createUser(any())).thenThrow(new InternalServerErrorHttpException("SQL interno"));
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno ao processar a requisição."))
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void handlesUnsupportedContentType() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.TEXT_PLAIN).content(REQUEST))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("O formato do conteúdo enviado não é suportado."));
        verifyNoInteractions(service);
    }

    @Test
    void rejectsEmptyBody() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O corpo da requisição está ausente ou contém um JSON inválido."));
        verifyNoInteractions(service);
    }

    @Test
    void exposesReadAndUpdateEndpoints() throws Exception {
        UUID id = UUID.randomUUID();
        var response = UserResponse.builder().userId(id).name("Maria").build();
        when(service.getUser(id)).thenReturn(response);
        when(service.updateUser(eq(id), any())).thenReturn(response);
        when(service.patchUser(eq(id), any())).thenReturn(response);
        mvc.perform(get("/users/{userId}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(id.toString()));
        mvc.perform(put("/users/{userId}", id).contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("OK"));
        mvc.perform(patch("/users/{userId}", id).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Novo Nome\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    void rejectsInvalidUserIdAndIncompletePut() throws Exception {
        mvc.perform(get("/users/not-a-uuid")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
        mvc.perform(put("/users/{userId}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void returnsBadRequestForInvalidUser() throws Exception {
        when(service.createUser(any())).thenThrow(new UserInvalidException("Usuário inválido."));
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Usuário inválido."))
                .andExpect(jsonPath("$.status").value("ERROR"));
    }
}
