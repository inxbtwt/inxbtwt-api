package com.inxbtwt.domain.dto.post.http;

import java.util.UUID;

public record PostDeletedResponse(UUID postId, PostResponse parent) {
}
