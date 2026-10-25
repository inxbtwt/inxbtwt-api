package com.inxbtwt.domain.dto.friendship;

import java.util.List;

public record FriendshipPageResponse(
        List<FriendshipResponse> content,
        int page,
        int size,
        boolean hasNext,
        boolean hasPrevious
) {
}
