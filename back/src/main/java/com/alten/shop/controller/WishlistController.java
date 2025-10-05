package com.alten.shop.controller;

import com.alten.shop.config.UserPrincipal;
import com.alten.shop.dto.response.ApiResponse;
import com.alten.shop.dto.response.WishlistResponse;
import com.alten.shop.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getWishlist(@AuthenticationPrincipal UserPrincipal currentUser) {
        WishlistResponse wishlist = wishlistService.getWishlistByUserId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Wishlist retrieved successfully", wishlist));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse> addToWishlist(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam Long productId) {

        WishlistResponse wishlist = wishlistService.addToWishlist(currentUser.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Product added to wishlist successfully", wishlist));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse> removeFromWishlist(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long productId) {

        WishlistResponse wishlist = wishlistService.removeFromWishlist(currentUser.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist successfully", wishlist));
    }
}