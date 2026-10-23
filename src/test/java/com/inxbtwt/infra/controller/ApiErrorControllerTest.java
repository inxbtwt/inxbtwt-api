package com.inxbtwt.infra.controller;

import com.inxbtwt.infra.exception.http.HttpMessages;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiErrorControllerTest {
    @Test
    void mapsErrorsOutsideControllersWithoutExposingTechnicalMessages() {
        var controller = new ApiErrorController();
        for (int code : new int[]{400, 401, 403, 404, 405, 406, 409, 413, 415, 429, 500, 503}) {
            var request = new MockHttpServletRequest();
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, code);
            request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "detalhe técnico");
            var response = controller.error(request);
            assertEquals(code, response.getStatusCode().value());
            assertEquals(HttpMessages.forStatus(response.getStatusCode()), response.getBody().message());
        }
    }
}
