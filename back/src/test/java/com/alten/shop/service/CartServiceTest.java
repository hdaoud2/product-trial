package com.alten.shop.service;

import com.alten.shop.dto.response.CartResponse;
import com.alten.shop.entity.Cart;
import com.alten.shop.entity.Product;
import com.alten.shop.entity.User;
import com.alten.shop.repository.CartRepository;
import com.alten.shop.repository.ProductRepository;
import com.alten.shop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void shouldAddToCart() {
        // Given
        Long userId = 1L;
        Long productId = 1L;
        Integer quantity = 2;

        User user = new User();
        user.setId(userId);

        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");

        Cart cart = new Cart();
        cart.setUser(user);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        CartResponse result = cartService.addToCart(userId, productId, quantity);

        // Then
        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }
}