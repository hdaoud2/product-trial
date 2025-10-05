package com.alten.shop.repository;

import com.alten.shop.entity.Cart;
import com.alten.shop.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindCartByUser() {
        // Given
        User user = createUser("cartuser@test.com", "cartuser");
        User savedUser = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(savedUser);

        // When
        Cart savedCart = cartRepository.save(cart);
        Optional<Cart> found = cartRepository.findByUser(savedUser);

        // Then
        assertTrue(found.isPresent());
        assertEquals(savedUser.getId(), found.get().getUser().getId());
    }

    @Test
    void shouldFindCartByUserId() {
        // Given
        User user = createUser("userid@test.com", "userid");
        User savedUser = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);

        // When
        Optional<Cart> found = cartRepository.findByUserId(savedUser.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(savedUser.getId(), found.get().getUser().getId());
    }

    @Test
    void shouldReturnEmpty_WhenCartNotFound() {
        // When
        Optional<Cart> found = cartRepository.findByUserId(999L);

        // Then
        assertFalse(found.isPresent());
    }

    private User createUser(String email, String username) {
        User user = new User();
        user.setUsername(username);
        user.setFirstname("Test");
        user.setEmail(email);
        user.setPassword("password123");
        return user;
    }
}