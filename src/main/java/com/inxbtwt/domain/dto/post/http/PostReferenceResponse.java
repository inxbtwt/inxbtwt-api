package com.inxbtwt.domain.dto.post.http;

import java.util.UUID;

public record PostReferenceResponse(UUID postId, String content, PostAuthorResponse author) {
}
