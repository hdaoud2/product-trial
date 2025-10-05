package com.alten.shop.controller;

import com.alten.shop.config.UserPrincipal;
import com.alten.shop.dto.request.ProductRequest;
import com.alten.shop.dto.response.ApiResponse;
import com.alten.shop.dto.response.ProductResponse;
import com.alten.shop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getProducts(
            @RequestParam Optional<String> category,
            @RequestParam Optional<Double> minPrice,
            @RequestParam Optional<Double> maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<ProductResponse> products = productService.getProducts(category, minPrice, maxPrice, pageable);

        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProduct(@PathVariable Long id) {
        ProductResponse product = productService.getProduct(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createProduct(
            @Valid @RequestBody ProductRequest productRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (!productService.isAdmin(currentUser.getEmail())) {
            throw new RuntimeException("Access denied. Admin privileges required.");
        }

        ProductResponse createdProduct = productService.createProduct(productRequest.toEntity());
        return ResponseEntity.ok(ApiResponse.success("Product created successfully", createdProduct));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest productRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (!productService.isAdmin(currentUser.getEmail())) {
            throw new RuntimeException("Access denied. Admin privileges required.");
        }

        ProductResponse updatedProduct = productService.updateProduct(id, productRequest.toEntity());
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updatedProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (!productService.isAdmin(currentUser.getEmail())) {
            throw new RuntimeException("Access denied. Admin privileges required.");
        }

        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }
}