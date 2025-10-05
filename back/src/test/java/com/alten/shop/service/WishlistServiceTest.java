package com.alten.shop.service;

import com.alten.shop.dto.response.WishlistResponse;
import com.alten.shop.entity.Product;
import com.alten.shop.entity.User;
import com.alten.shop.entity.Wishlist;
import com.alten.shop.repository.ProductRepository;
import com.alten.shop.repository.UserRepository;
import com.alten.shop.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistService wishlistService;

    private final Long USER_ID = 1L;
    private final Long PRODUCT_ID = 1L;
    private final Long PRODUCT_ID_2 = 2L;

    // Helper methods to create test data
    private User createTestUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setFirstname("Test");
        return user;
    }

    private Product createTestProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product " + id);
        product.setPrice(99.99);
        product.setCategory("Electronics");
        product.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        return product;
    }

    private Wishlist createTestWishlist(User user, Set<Product> products) {
        Wishlist wishlist = new Wishlist();
        wishlist.setId(1L);
        wishlist.setUser(user);
        wishlist.setProducts(products != null ? new LinkedHashSet<>(products) : new LinkedHashSet<>());
        wishlist.setUpdatedAt(LocalDateTime.now());
        return wishlist;
    }

    @Test
    void shouldGetWishlistByUserId_WhenWishlistExists() {
        // Given
        User user = createTestUser();
        Product product = createTestProduct(PRODUCT_ID);
        Wishlist wishlist = createTestWishlist(user, new LinkedHashSet<>(java.util.List.of(product)));

        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));

        // When
        WishlistResponse result = wishlistService.getWishlistByUserId(USER_ID);

        // Then
        assertNotNull(result);
        assertEquals(1, result.products().size());
        assertEquals("Test Product 1", result.products().get(0).name());
        verify(wishlistRepository).findByUserId(USER_ID);
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void shouldCreateNewWishlist_WhenWishlistDoesNotExist() {
        // Given
        User user = createTestUser();
        Wishlist newWishlist = createTestWishlist(user, new LinkedHashSet<>());

        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(newWishlist);

        // When
        WishlistResponse result = wishlistService.getWishlistByUserId(USER_ID);

        // Then
        assertNotNull(result);
        assertTrue(result.products().isEmpty());
        verify(wishlistRepository).findByUserId(USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void shouldAddToWishlist_WhenProductNotInWishlist() {
        // Given
        User user = createTestUser();
        Product product = createTestProduct(PRODUCT_ID);
        Wishlist wishlist = createTestWishlist(user, new LinkedHashSet<>());

        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        // When
        WishlistResponse result = wishlistService.addToWishlist(USER_ID, PRODUCT_ID);

        // Then
        assertNotNull(result);
        assertEquals(1, result.products().size());
        assertEquals(PRODUCT_ID, result.products().get(0).id());
        verify(wishlistRepository).findByUserId(USER_ID);
        verify(productRepository).findById(PRODUCT_ID);
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    void shouldThrowException_WhenAddingProductAlreadyInWishlist() {
        // Given
        User user = createTestUser();
        Product product = createTestProduct(PRODUCT_ID);
        Wishlist wishlist = createTestWishlist(user, new LinkedHashSet<>(java.util.List.of(product)));

        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> wishlistService.addToWishlist(USER_ID, PRODUCT_ID));

        assertEquals("Product already in wishlist", exception.getMessage());
        verify(wishlistRepository).findByUserId(USER_ID);
        verify(productRepository).findById(PRODUCT_ID);
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void shouldThrowException_WhenAddingNonExistentProduct() {
        // Given
        User user = createTestUser();
        Wishlist wishlist = createTestWishlist(user, new LinkedHashSet<>());

        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> wishlistService.addToWishlist(USER_ID, PRODUCT_ID));

        assertEquals("Product not found", exception.getMessage());
        verify(wishlistRepository).findByUserId(USER_ID);
        verify(productRepository).findById(PRODUCT_ID);
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void shouldRemoveFromWishlist_WhenProductExists() {
        // Given
        User user = createTestUser();
        Product product1 = createTestProduct(PRODUCT_ID);
        Product product2 = createTestProduct(PRODUCT_ID_2);
        Wishlist wishlist = createTestWishlist(user, new LinkedHashSet<>(java.util.List.of(product1, product2)));

        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> {
            Wishlist saved = invocation.getArgument(0);
            saved.getProducts().removeIf(p -> p.getId().equals(PRODUCT_ID));
            return saved;
        });

        // When
        WishlistResponse result = wishlistService.removeFromWishlist(USER_ID, PRODUCT_ID);

        // Then
        assertNotNull(result);
        assertEquals(1, result.products().size());
        assertEquals(PRODUCT_ID_2, result.products().get(0).id());
        verify(wishlistRepository).findByUserId(USER_ID);
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    void shouldRemoveFromWishlist_WhenProductDoesNotExistInWishlist() {
        // Given
        User user = createTestUser();
        Product product1 = createTestProduct(PRODUCT_ID);
        Wishlist wishlist = createTestWishlist(user, new LinkedHashSet<>(java.util.List.of(product1)));

        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);

        // When
        WishlistResponse result = wishlistService.removeFromWishlist(USER_ID, PRODUCT_ID_2);

        // Then
        assertNotNull(result);
        assertEquals(1, result.products().size()); // Still has product1
        verify(wishlistRepository).findByUserId(USER_ID);
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    void shouldHandleRemoveFromNonExistentWishlist() {
        // Given
        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createTestUser()));

        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        // When & Then
        assertDoesNotThrow(() -> {
            WishlistResponse result = wishlistService.removeFromWishlist(USER_ID, PRODUCT_ID);
            assertNotNull(result);
            assertTrue(result.products().isEmpty());
        });

        verify(wishlistRepository).findByUserId(USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(wishlistRepository, times(2)).save(any(Wishlist.class));
    }

    @Test
    void shouldCreateNewWishlist_WhenAddingToNonExistentWishlist() {
        // Given
        User user = createTestUser();
        Product product = createTestProduct(PRODUCT_ID);

        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        // When
        WishlistResponse result = wishlistService.addToWishlist(USER_ID, PRODUCT_ID);

        // Then
        assertNotNull(result);
        assertEquals(1, result.products().size());
        assertEquals(PRODUCT_ID, result.products().get(0).id());

        verify(wishlistRepository).findByUserId(USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(productRepository).findById(PRODUCT_ID);
        verify(wishlistRepository, times(2)).save(any(Wishlist.class));
    }

    @Test
    void shouldHandleEmptyWishlist() {
        // Given
        User user = createTestUser();
        Wishlist emptyWishlist = createTestWishlist(user, new LinkedHashSet<>());

        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyWishlist));

        // When
        WishlistResponse result = wishlistService.getWishlistByUserId(USER_ID);

        // Then
        assertNotNull(result);
        assertTrue(result.products().isEmpty());
        verify(wishlistRepository).findByUserId(USER_ID);
    }

    @Test
    void shouldHandleMultipleProductsInWishlist() {
        // Given
        User user = createTestUser();
        Product product1 = createTestProduct(PRODUCT_ID);
        Product product2 = createTestProduct(PRODUCT_ID_2);
        Wishlist wishlist = createTestWishlist(user, new LinkedHashSet<>(java.util.List.of(product1, product2)));

        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));

        // When
        WishlistResponse result = wishlistService.getWishlistByUserId(USER_ID);

        // Then
        assertNotNull(result);
        assertEquals(2, result.products().size());
        assertEquals(PRODUCT_ID, result.products().get(0).id());
        assertEquals(PRODUCT_ID_2, result.products().get(1).id());
        verify(wishlistRepository).findByUserId(USER_ID);
    }

    @Test
    void shouldThrowException_WhenUserNotFoundForNewWishlist() {
        // Given
        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> wishlistService.getWishlistByUserId(USER_ID));

        assertEquals("User not found", exception.getMessage());
        verify(wishlistRepository).findByUserId(USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }
}