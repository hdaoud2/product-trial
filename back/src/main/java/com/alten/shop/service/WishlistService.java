package com.alten.shop.service;

import com.alten.shop.dto.response.ProductResponse;
import com.alten.shop.dto.response.WishlistResponse;
import com.alten.shop.entity.Product;
import com.alten.shop.entity.User;
import com.alten.shop.entity.Wishlist;
import com.alten.shop.repository.ProductRepository;
import com.alten.shop.repository.UserRepository;
import com.alten.shop.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public WishlistService(WishlistRepository wishlistRepository, UserRepository userRepository,
                           ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public WishlistResponse getWishlistByUserId(Long userId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> createNewWishlist(userId));

        return mapToWishlistResponse(wishlist);
    }

    public WishlistResponse addToWishlist(Long userId, Long productId) {
        Wishlist wishlist = getWishlistEntityByUserId(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (wishlist.getProducts().stream().anyMatch(p -> p.getId().equals(productId))) {
            throw new RuntimeException("Product already in wishlist");
        }

        wishlist.getProducts().add(product);
        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        return mapToWishlistResponse(savedWishlist);
    }

    public WishlistResponse removeFromWishlist(Long userId, Long productId) {
        Wishlist wishlist = getWishlistEntityByUserId(userId);
        wishlist.getProducts().removeIf(product -> product.getId().equals(productId));
        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        return mapToWishlistResponse(savedWishlist);
    }

    private Wishlist getWishlistEntityByUserId(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> createNewWishlist(userId));
    }

    private Wishlist createNewWishlist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Wishlist newWishlist = new Wishlist();
        newWishlist.setUser(user);
        return wishlistRepository.save(newWishlist);
    }

    private WishlistResponse mapToWishlistResponse(Wishlist wishlist) {
        List<ProductResponse> products = wishlist.getProducts().stream()
                .map(ProductResponse::fromEntity)
                .toList();

        return new WishlistResponse(wishlist.getId(), products, wishlist.getUpdatedAt());
    }
}