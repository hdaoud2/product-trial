package com.alten.shop.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        Long id,
        List<CartItemResponse> items,
        LocalDateTime updatedAt
) {
}