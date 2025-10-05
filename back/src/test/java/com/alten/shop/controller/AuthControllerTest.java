package com.alten.shop.controller;

import com.alten.shop.config.AuthEntryPointJwt;
import com.alten.shop.config.JwtUtils;
import com.alten.shop.config.SecurityConfig;
import com.alten.shop.dto.request.AuthRequest;
import com.alten.shop.dto.request.LoginRequest;
import com.alten.shop.dto.response.AuthResponse;
import com.alten.shop.dto.response.UserResponse;
import com.alten.shop.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.security.enabled=true")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    UserDetailsService userDetailsService;
    @MockitoBean
    PasswordEncoder passwordEncoder;
    @MockitoBean
    AuthEntryPointJwt unauthorizedHandler;
    @MockitoBean
    JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterUser() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest(
                "testuser", "Test User", "test@test.com", "password123"
        );

        UserResponse userResponse = new UserResponse(1L, "testuser", "Test User", "test@test.com");
        AuthResponse authResponse = new AuthResponse("jwt-token", userResponse);

        when(authService.register(any(AuthRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"));
    }

    @Test
    void shouldLoginUser() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test@test.com", "password123");

        UserResponse userResponse = new UserResponse(1L, "testuser", "Test User", "test@test.com");
        AuthResponse authResponse = new AuthResponse("jwt-token", userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.email").value("test@test.com"));
    }

    @Test
    void shouldReturnBadRequest_WhenInvalidRegistrationData() throws Exception {
        // Given - Invalid email format
        AuthRequest invalidRequest = new AuthRequest(
                "user", "Test", "invalid-email", "short"
        );

        // When & Then
        mockMvc.perform(post("/auth/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_WhenInvalidLoginData() throws Exception {
        // Given - Invalid email format
        LoginRequest invalidRequest = new LoginRequest("invalid-email", "");

        // When & Then
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleServiceException_WhenRegistrationFails() throws Exception {
        var authRequest = new AuthRequest("testuser", "Test User", "test@test.com", "password123");

        when(authService.register(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Email already in use"));

        assertThatThrownBy(() ->
                mockMvc.perform(post("/auth/account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
        )
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already in use");
    }

}