package com.inxbtwt.domain.dto.user.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserResponse(
    UUID userId,
    String name,

    @JsonIgnore
    String photoUrl,
    String email,
    String phone,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) implements Serializable {
}
