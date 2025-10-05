package com.alten.shop.repository;

import com.alten.shop.entity.User;
import com.alten.shop.entity.Wishlist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WishlistRepositoryTest {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindWishlistByUser() {
        // Given
        User user = createUser("wishlist@test.com", "wishlistuser");
        User savedUser = userRepository.save(user);

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(savedUser);

        // When
        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        Optional<Wishlist> found = wishlistRepository.findByUser(savedUser);

        // Then
        assertTrue(found.isPresent());
        assertEquals(savedUser.getId(), found.get().getUser().getId());
    }

    @Test
    void shouldFindWishlistByUserId() {
        // Given
        User user = createUser("wishuser@test.com", "wishuser");
        User savedUser = userRepository.save(user);

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(savedUser);
        wishlistRepository.save(wishlist);

        // When
        Optional<Wishlist> found = wishlistRepository.findByUserId(savedUser.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(savedUser.getId(), found.get().getUser().getId());
    }

    @Test
    void shouldReturnEmpty_WhenWishlistNotFound() {
        // When
        Optional<Wishlist> found = wishlistRepository.findByUserId(999L);

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