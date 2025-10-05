package com.alten.shop.repository;

import com.alten.shop.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setFirstname("Test");
        user.setEmail("test@test.com");
        user.setPassword("password123");

        // When
        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@test.com", found.get().getEmail());
    }

    @Test
    void shouldFindByEmail() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setFirstname("Test");
        user.setEmail("unique@test.com");
        user.setPassword("password123");
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("unique@test.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void shouldFindByUsername() {
        // Given
        User user = new User();
        user.setUsername("uniqueuser");
        user.setFirstname("Test");
        user.setEmail("test@test.com");
        user.setPassword("password123");
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByUsername("uniqueuser");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test@test.com", found.get().getEmail());
    }

    @Test
    void shouldCheckIfEmailExists() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setFirstname("Test");
        user.setEmail("existing@test.com");
        user.setPassword("password123");
        userRepository.save(user);

        // When & Then
        assertTrue(userRepository.existsByEmail("existing@test.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@test.com"));
    }

    @Test
    void shouldCheckIfUsernameExists() {
        // Given
        User user = new User();
        user.setUsername("existinguser");
        user.setFirstname("Test");
        user.setEmail("test@test.com");
        user.setPassword("password123");
        userRepository.save(user);

        // When & Then
        assertTrue(userRepository.existsByUsername("existinguser"));
        assertFalse(userRepository.existsByUsername("nonexistentuser"));
    }
}