package com.alten.shop.service;

import com.alten.shop.dto.response.CartItemResponse;
import com.alten.shop.dto.response.CartResponse;
import com.alten.shop.dto.response.ProductResponse;
import com.alten.shop.entity.Cart;
import com.alten.shop.entity.CartItem;
import com.alten.shop.entity.Product;
import com.alten.shop.entity.User;
import com.alten.shop.repository.CartRepository;
import com.alten.shop.repository.ProductRepository;
import com.alten.shop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public CartResponse getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        return mapToCartResponse(cart);
    }

    public CartResponse addToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = getCartEntityByUserId(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    public CartResponse updateCartItem(Long userId, Long productId, Integer quantity) {
        if (quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }

        Cart cart = getCartEntityByUserId(userId);
        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        item.setQuantity(quantity);
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    public CartResponse removeFromCart(Long userId, Long productId) {
        Cart cart = getCartEntityByUserId(userId);
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    public void clearCart(Long userId) {
        Cart cart = getCartEntityByUserId(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getCartEntityByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    private Cart createNewCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart newCart = new Cart();
        newCart.setUser(user);
        return cartRepository.save(newCart);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        ProductResponse.fromEntity(item.getProduct()),
                        item.getQuantity()
                ))
                .toList();

        return new CartResponse(cart.getId(), items, cart.getUpdatedAt());
    }
}