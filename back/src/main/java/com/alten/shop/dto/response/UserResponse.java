package com.alten.shop.dto.response;

import com.alten.shop.entity.User;

public record UserResponse(
        Long id,
        String username,
        String firstname,
        String email
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstname(),
                user.getEmail()
        );
    }
}