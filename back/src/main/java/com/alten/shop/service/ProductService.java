package com.alten.shop.service;

import com.alten.shop.dto.response.ProductResponse;
import com.alten.shop.entity.Product;
import com.alten.shop.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductResponse> getProducts(Optional<String> category, Optional<Double> minPrice,
                                             Optional<Double> maxPrice, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            category.ifPresent(cat -> predicates.add(cb.equal(root.get("category"), cat)));
            minPrice.ifPresent(min -> predicates.add(cb.greaterThanOrEqualTo(root.get("price"), min)));
            maxPrice.ifPresent(max -> predicates.add(cb.lessThanOrEqualTo(root.get("price"), max)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(ProductResponse::fromEntity);
    }

    public Optional<ProductResponse> getProduct(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::fromEntity);
    }

    public ProductResponse createProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        return ProductResponse.fromEntity(savedProduct);
    }

    public ProductResponse updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setCode(productDetails.getCode());
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setImage(productDetails.getImage());
        product.setCategory(productDetails.getCategory());
        product.setPrice(productDetails.getPrice());
        product.setQuantity(productDetails.getQuantity());
        product.setInternalReference(productDetails.getInternalReference());
        product.setShellId(productDetails.getShellId());
        product.setInventoryStatus(productDetails.getInventoryStatus());
        product.setRating(productDetails.getRating());

        Product updatedProduct = productRepository.save(product);
        return ProductResponse.fromEntity(updatedProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    public boolean isAdmin(String email) {
        return "admin@admin.com".equals(email);
    }
}