package com.inxbtwt.domain.dto.user.http;

import lombok.Builder;
import java.io.Serializable;

/** Null fields are unchanged; the merged data is validated before saving. */
@Builder
public record PatchUserRequest(
        String name,
        String email,
        String phone,
        String password
) implements Serializable {
}
