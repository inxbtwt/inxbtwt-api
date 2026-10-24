package com.inxbtwt.domain.dto.user.http;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record AddressResponse(
        UUID addressId,
        String street,
        String city,
        String state,
        String zipCode,
        String country,
        String referenceName,
        String number,
        boolean isDefault,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {
}
