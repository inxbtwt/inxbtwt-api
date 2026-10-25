package com.inxbtwt.domain.service.friendship;

import com.inxbtwt.domain.dto.friendship.FriendshipPageResponse;
import com.inxbtwt.domain.dto.friendship.FriendshipResponse;
import com.inxbtwt.domain.enums.FriendRequestDirection;

import java.util.UUID;

public interface FriendshipService {

    FriendshipResponse getState(UUID userId);

    FriendshipPageResponse getFriends(int page, int size);

    FriendshipPageResponse getRequests(FriendRequestDirection direction, int page, int size);

    long countFriends();

    FriendshipResponse request(UUID userId);

    FriendshipResponse accept(UUID userId);

    FriendshipResponse decline(UUID userId);

    FriendshipResponse cancel(UUID userId);

    FriendshipResponse remove(UUID userId);
}
