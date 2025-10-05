package com.alten.shop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank String username,
        @NotBlank String firstname,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password
) {
}