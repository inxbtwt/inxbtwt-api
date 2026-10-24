package com.inxbtwt.domain.dto.user.http;

import lombok.Builder;
import java.io.Serializable;

@Builder
public record PatchUserRequest(
        String name,
        String username,
        String email,
        String phone,
        String password
) implements Serializable {
}
