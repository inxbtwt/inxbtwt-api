package com.inxbtwt.infra.exception.http;

import com.inxbtwt.infra.controller.notification.NotificationController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = NotificationController.class)
public class NotificationExceptionHandler extends UserExceptionHandler {
}
