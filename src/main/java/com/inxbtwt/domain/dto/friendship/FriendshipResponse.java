package com.inxbtwt.domain.dto.friendship;

import com.inxbtwt.domain.enums.FriendshipState;

import java.time.LocalDateTime;

public record FriendshipResponse(
        FriendUserResponse user,
        FriendshipState state,
        LocalDateTime requestedAt,
        LocalDateTime acceptedAt
) {
}
