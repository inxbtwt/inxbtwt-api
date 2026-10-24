package com.inxbtwt.infra.exception.http;

import com.inxbtwt.infra.controller.friendship.FriendshipController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = FriendshipController.class)
public class FriendshipExceptionHandler extends UserExceptionHandler {
}
