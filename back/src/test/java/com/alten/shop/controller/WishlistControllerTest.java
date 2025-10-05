package com.alten.shop.controller;

import com.alten.shop.config.AuthEntryPointJwt;
import com.alten.shop.config.JwtUtils;
import com.alten.shop.config.SecurityConfig;
import com.alten.shop.config.UserPrincipal;
import com.alten.shop.service.WishlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.security.enabled=true")
class WishlistControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WishlistService wishlistService;
    @MockitoBean
    UserDetailsService userDetailsService;
    @MockitoBean
    PasswordEncoder passwordEncoder;
    @MockitoBean
    AuthEntryPointJwt unauthorizedHandler;
    @MockitoBean
    JwtUtils jwtUtils;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void stubUnauthorizedEntryPoint() throws Exception {
        doAnswer(inv -> {
            HttpServletResponse resp = inv.getArgument(1);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }).when(unauthorizedHandler).commence(any(), any(), any());
    }

    private RequestPostProcessor withUserPrincipal(UserPrincipal principal) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var auth = new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
        return authentication(auth);
    }

    @Test
    void shouldGetWishlist() throws Exception {
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(123L);
        when(principal.getUsername()).thenReturn("test@test.com");

        mockMvc.perform(get("/wishlist").with(withUserPrincipal(principal)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddToWishlist() throws Exception {
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(123L);
        when(principal.getUsername()).thenReturn("test@test.com");

        mockMvc.perform(post("/wishlist/items")
                        .with(csrf())
                        .with(withUserPrincipal(principal))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("productId", "42"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUnauthorized_WhenAccessingWishlistWithoutAuth() throws Exception {
        mockMvc.perform(get("/wishlist"))
                .andExpect(status().isUnauthorized());
    }
}
