package com.example.polymarket.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PricePointResponse(
        Long outcomeId,
        String outcomeLabel,
        BigDecimal price,
        Instant recordedAt
) {
}
