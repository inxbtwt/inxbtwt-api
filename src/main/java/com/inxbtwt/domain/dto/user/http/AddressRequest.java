package com.inxbtwt.domain.dto.user.http;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record AddressRequest(

        @NotBlank
        @Size(max = 255)
        String street,

        @NotBlank
        @Size(max = 255)
        String city,

        @NotBlank
        @Size(max = 255)
        String state,

        @NotBlank
        @Size(max = 255)
        String zipCode,

        @NotBlank
        @Size(max = 255)
        String country,

        @NotBlank
        @Size(max = 255)
        String referenceName,

        @Size(max = 255)
        String number,

        boolean isDefault
) implements Serializable {
}
