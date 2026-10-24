package com.inxbtwt.infra.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.security")
public record SecurityRoutesProperties(
        @NotEmpty
        List<@NotNull @Valid Route> routes
) {
    public record Route(
            @NotBlank
            String path,

            @NotBlank
            @Pattern(regexp = "GET|HEAD|POST|PUT|PATCH|DELETE|OPTIONS|TRACE", message = "O método HTTP deve ser válido e estar em maiúsculas.")
            String method,

            @NotNull
            Access access,

            List<@NotBlank String> roles
    ) {
        public Route {
            roles = roles == null ? List.of() : List.copyOf(roles);
        }

        @AssertTrue(message = "Informe roles apenas quando access for ROLES e inclua ao menos uma role.")
        public boolean isRolesConfigurationValid() {
            return (access == Access.ROLES) != roles.isEmpty();
        }
    }

    public enum Access {
        PERMIT_ALL,
        AUTHENTICATED,
        ROLES
    }
}
