package com.inxbtwt.infra.exception.http;

import org.springframework.http.HttpStatusCode;

public final class HttpMessages {
    public static final String PROFILES_FOUND = "Perfis consultados com sucesso.";
    public static final String PROFILE_FOUND = "Perfil consultado com sucesso.";
    public static final String PROFILE_UPDATED = "Perfil atualizado com sucesso.";
    public static final String USER_AVAILABILITY_CHECKED = "Disponibilidade consultada com sucesso.";
    public static final String USER_PHOTOS_FOUND = "URLs das fotos do usuário consultadas com sucesso.";
    public static final String USER_PHOTO_UPDATED = "Foto do usuário atualizada com sucesso.";
    public static final String USER_CREATED = "Usuário criado com sucesso.";
    public static final String USER_FOUND = "Usuário consultado com sucesso.";
    public static final String USERS_FOUND = "Usuários consultados com sucesso.";
    public static final String USER_UPDATED = "Usuário atualizado com sucesso.";
    public static final String BAD_REQUEST = "Os dados da requisição são inválidos.";
    public static final String NOT_FOUND = "O recurso solicitado não foi encontrado.";
    public static final String CONFLICT = "O recurso já está cadastrado.";
    public static final String INVALID_BODY = "O corpo da requisição está ausente ou contém um JSON inválido.";
    public static final String INTERNAL_SERVER_ERROR = "Ocorreu um erro interno ao processar a requisição.";
    public static final String UNAUTHORIZED = "É necessário autenticar-se para acessar este recurso.";
    public static final String FORBIDDEN = "Você não tem permissão para acessar este recurso.";
    public static final String METHOD_NOT_ALLOWED = "O método HTTP utilizado não é permitido para este recurso.";
    public static final String NOT_ACCEPTABLE = "O formato de resposta solicitado não é suportado.";
    public static final String PAYLOAD_TOO_LARGE = "O conteúdo enviado excede o tamanho permitido.";
    public static final String UNSUPPORTED_MEDIA_TYPE = "O formato do conteúdo enviado não é suportado.";
    public static final String TOO_MANY_REQUESTS = "Muitas requisições foram enviadas. Tente novamente mais tarde.";
    public static final String SERVICE_UNAVAILABLE = "O serviço está temporariamente indisponível. Tente novamente mais tarde.";

    public static String forStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 401 -> UNAUTHORIZED;
            case 403 -> FORBIDDEN;
            case 404 -> NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 406 -> NOT_ACCEPTABLE;
            case 409 -> CONFLICT;
            case 413 -> PAYLOAD_TOO_LARGE;
            case 415 -> UNSUPPORTED_MEDIA_TYPE;
            case 429 -> TOO_MANY_REQUESTS;
            case 503 -> SERVICE_UNAVAILABLE;
            default -> status.is5xxServerError() ? INTERNAL_SERVER_ERROR : BAD_REQUEST;
        };
    }

    private HttpMessages() {
    }
}
