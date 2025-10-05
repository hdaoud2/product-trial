package com.alten.shop.dto.request;

import com.alten.shop.entity.Product;
import jakarta.validation.constraints.*;

public record ProductRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String image,
        @NotBlank String category,
        @NotNull @Positive Double price,
        @NotNull @Min(0) Integer quantity,
        @NotBlank String internalReference,
        @NotNull Long shellId,
        Product.InventoryStatus inventoryStatus,
        @Min(0) @Max(5) Integer rating
) {
    public Product toEntity() {
        Product product = new Product();
        product.setCode(this.code);
        product.setName(this.name);
        product.setDescription(this.description);
        product.setImage(this.image);
        product.setCategory(this.category);
        product.setPrice(this.price);
        product.setQuantity(this.quantity);
        product.setInternalReference(this.internalReference);
        product.setShellId(this.shellId);
        product.setInventoryStatus(this.inventoryStatus);
        product.setRating(this.rating);
        return product;
    }
}