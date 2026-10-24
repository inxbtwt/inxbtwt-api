package com.inxbtwt.domain.dto.post.http;

import java.util.List;

public record PostPageResponse(
        List<PostResponse> content,
        int page,
        int size,
        boolean hasNext,
        boolean hasPrevious
) {
}
