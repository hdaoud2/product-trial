package com.alten.shop.service;

import com.alten.shop.dto.response.ProductResponse;
import com.alten.shop.entity.Product;
import com.alten.shop.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldReturnProductsWithFilters() {
        // Given
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        Page<Product> expectedPage = new PageImpl<>(Arrays.asList(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        // When
        Page<ProductResponse> result = productService.getProducts(
                Optional.of("Electronics"),
                Optional.of(10.0),
                Optional.of(100.0),
                Pageable.unpaged()
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void shouldCreateProduct() {
        // Given
        Product product = new Product();
        product.setName("New Product");

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse result = productService.createProduct(product);

        // Then
        assertNotNull(result);
        assertEquals("New Product", result.name());
        verify(productRepository).save(product);
    }
}