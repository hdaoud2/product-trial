package com.alten.shop.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record WishlistResponse(
        Long id,
        List<ProductResponse> products,
        LocalDateTime updatedAt
) {
}