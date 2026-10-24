package com.inxbtwt.domain.mapper.user;

import com.inxbtwt.domain.dto.user.http.AddressRequest;
import com.inxbtwt.domain.dto.user.http.CreateUserRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void mapsAddressesAndLinksThemToTheirOwner() {
        var address = new AddressRequest("Rua A", "São Paulo", "SP", "01001000",
                "Brasil", "Casa", "10", true);
        var request = new CreateUserRequest("Maria", "maria@example.com", "11999999999",
                "password123", List.of(address));

        var user = mapper.toEntity(request);

        assertThat(user.getAddresses()).hasSize(1);
        assertThat(user.getAddresses().getFirst().getUser()).isSameAs(user);
        assertThat(user.getAddresses().getFirst().isDefault()).isTrue();
        var response = mapper.toResponse(user).addresses().getFirst();
        assertThat(response.street()).isEqualTo("Rua A");
        assertThat(response.isDefault()).isTrue();
    }

    @Test
    void missingAddressesProducesEmptyCollection() {
        var user = mapper.toEntity(new CreateUserRequest("Maria", "maria@example.com",
                "11999999999", "password123", null));
        assertThat(user.getAddresses()).isEmpty();
        assertThat(mapper.toResponse(user).addresses()).isEmpty();
    }

    @Test
    void validatesNestedAddresses() {
        var address = new AddressRequest("", "São Paulo", "SP", "01001000",
                "Brasil", "Casa", null, false);
        var request = new CreateUserRequest("Maria", "maria@example.com", "11999999999",
                "password123", List.of(address));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(request))
                    .anyMatch(violation -> violation.getPropertyPath().toString()
                            .equals("addresses[0].street"));
        }
    }
}
