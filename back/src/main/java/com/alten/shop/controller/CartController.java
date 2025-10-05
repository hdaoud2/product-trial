package com.alten.shop.controller;

import com.alten.shop.config.UserPrincipal;
import com.alten.shop.dto.request.CartItemRequest;
import com.alten.shop.dto.response.ApiResponse;
import com.alten.shop.dto.response.CartResponse;
import com.alten.shop.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getCart(@AuthenticationPrincipal UserPrincipal currentUser) {
        CartResponse cart = cartService.getCartByUserId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse> addToCart(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CartItemRequest request) {

        CartResponse cart = cartService.addToCart(currentUser.getId(), request.productId(), request.quantity());
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cart));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse> updateCartItem(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        CartResponse cart = cartService.updateCartItem(currentUser.getId(), productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse> removeFromCart(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long productId) {

        CartResponse cart = cartService.removeFromCart(currentUser.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", cart));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCart(@AuthenticationPrincipal UserPrincipal currentUser) {
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }
}