package com.alten.shop.dto.response;

import java.util.Objects;

public record AuthResponse(
        String token,
        String type,
        UserResponse user
) {
    public AuthResponse(String token, UserResponse user) {
        this(token, "Bearer", user);
    }

    public AuthResponse {
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(user, "user must not be null");
    }
}
