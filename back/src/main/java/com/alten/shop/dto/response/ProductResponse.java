package com.alten.shop.dto.response;

import com.alten.shop.entity.Product;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String code,
        String name,
        String description,
        String image,
        String category,
        Double price,
        Integer quantity,
        String internalReference,
        Long shellId,
        Product.InventoryStatus inventoryStatus,
        Integer rating,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse fromEntity(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getCode(),
                p.getName(),
                p.getDescription(),
                p.getImage(),
                p.getCategory(),
                p.getPrice(),
                p.getQuantity(),
                p.getInternalReference(),
                p.getShellId(),
                p.getInventoryStatus(),
                p.getRating(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
