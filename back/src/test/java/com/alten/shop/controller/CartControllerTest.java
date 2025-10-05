package com.alten.shop.controller;

import com.alten.shop.config.AuthEntryPointJwt;
import com.alten.shop.config.JwtUtils;
import com.alten.shop.config.SecurityConfig;
import com.alten.shop.config.UserPrincipal;
import com.alten.shop.dto.request.CartItemRequest;
import com.alten.shop.dto.response.CartItemResponse;
import com.alten.shop.dto.response.CartResponse;
import com.alten.shop.dto.response.ProductResponse;
import com.alten.shop.entity.Product;
import com.alten.shop.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.security.enabled=true")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private AuthEntryPointJwt unauthorizedHandler;
    @MockitoBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void stubUnauthorizedEntryPoint() throws ServletException, IOException {
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

    private CartResponse createTestCartResponse() {
        ProductResponse productResponse = new ProductResponse(
                1L, "TEST123", "Test Product", "Description",
                "image.jpg", "Electronics", 99.99, 10,
                "REF-123", 1L, Product.InventoryStatus.INSTOCK,
                5, LocalDateTime.now(), LocalDateTime.now()
        );
        CartItemResponse cartItem = new CartItemResponse(1L, productResponse, 2);
        return new CartResponse(1L, Arrays.asList(cartItem), LocalDateTime.now());
    }

    @Test
    void shouldGetCart() throws Exception {
        var principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(123L);
        when(principal.getUsername()).thenReturn("test@test.com");

        CartResponse cartResponse = createTestCartResponse();
        when(cartService.getCartByUserId(123L)).thenReturn(cartResponse);

        mockMvc.perform(get("/cart").with(withUserPrincipal(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));
    }

    @Test
    void shouldAddToCart() throws Exception {
        var principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(123L);
        when(principal.getUsername()).thenReturn("test@test.com");

        CartItemRequest cartItemRequest = new CartItemRequest(1L, 2);
        CartResponse cartResponse = createTestCartResponse();
        when(cartService.addToCart(123L, 1L, 2)).thenReturn(cartResponse);

        mockMvc.perform(post("/cart/items")
                        .with(csrf())
                        .with(withUserPrincipal(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item added to cart successfully"));
    }

    @Test
    void shouldReturnUnauthorized_WhenAccessingCartWithoutAuth() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isUnauthorized());
    }
}
