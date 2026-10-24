package com.inxbtwt.domain.dto.user.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Builder
public record UserResponse(
    UUID userId,
    String name,
    String email,
    String phone,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<AddressResponse> addresses
) implements Serializable {
}
