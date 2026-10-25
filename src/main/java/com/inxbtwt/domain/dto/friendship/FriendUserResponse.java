package com.inxbtwt.domain.dto.friendship;

import java.util.UUID;

public record FriendUserResponse(UUID userId, String name, String username, String photoUrl) {
}
