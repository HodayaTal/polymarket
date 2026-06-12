package com.example.polymarket.dto;

import com.example.polymarket.domain.TradeSide;
import java.math.BigDecimal;
import java.time.Instant;

public record TradeResponse(
        Long id,
        Long userId,
        String username,
        Long marketId,
        String marketQuestion,
        Long outcomeId,
        String outcomeLabel,
        TradeSide side,
        BigDecimal units,
        BigDecimal price,
        BigDecimal total,
        BigDecimal balanceAfter,
        Instant createdAt
) {
}
