package com.alten.shop.controller;

import com.alten.shop.dto.request.ProductRequest;
import com.alten.shop.dto.response.ProductResponse;
import com.alten.shop.entity.Product;
import com.alten.shop.service.ProductService;
import com.alten.shop.util.TestSecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldGetProducts() throws Exception {
        ProductResponse product = new ProductResponse(
                1L, null, "Test Product", null, null, null, null, null, null, null, null, null, null, null
        );

        Page<ProductResponse> productPage = new PageImpl<>(java.util.List.of(product));
        when(productService.getProducts(any(), any(), any(), any())).thenReturn(productPage);

        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // Page is wrapped in ApiResponse.data
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"));
    }

    @Test
    @WithMockUser
    void shouldGetProductById() throws Exception {
        ProductResponse product = new ProductResponse(
                1L, null, "Test Product", null, null, null, null, null, null, null, null, null, null, null
        );

        when(productService.getProduct(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // Single item is under ApiResponse.data
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    void shouldCreateProduct() throws Exception {
        TestSecurityUtils.setAuthUser("admin@admin.com", "admin");

        try {
            // Given
            ProductRequest productRequest = new ProductRequest(
                    "TEST123", "New Product", "Test Description", "test.jpg",
                    "Electronics", 100.0, 10, "REF-123", 1L,
                    Product.InventoryStatus.INSTOCK, 5
            );

            ProductResponse productResponse = new ProductResponse(
                    1L, "TEST123", "New Product", "Test Description", "test.jpg",
                    "Electronics", 100.0, 10, "REF-123", 1L,
                    Product.InventoryStatus.INSTOCK, 5, LocalDateTime.now(), LocalDateTime.now()
            );

            when(productService.isAdmin("admin@admin.com")).thenReturn(true);
            when(productService.createProduct(any(Product.class))).thenReturn(productResponse);

            // When & Then
            mockMvc.perform(post("/products")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("New Product"));
        } finally {
            TestSecurityUtils.clearAuth();
        }
    }
}