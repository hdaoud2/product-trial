package com.alten.shop.dto.response;

public record CartItemResponse(
        Long id,
        ProductResponse product,
        Integer quantity
) {
}