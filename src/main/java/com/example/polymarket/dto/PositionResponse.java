package com.example.polymarket.dto;

import java.math.BigDecimal;

public record PositionResponse(
        Long marketId,
        String marketQuestion,
        Long outcomeId,
        String outcomeLabel,
        BigDecimal units,
        BigDecimal averageEntryPrice,
        BigDecimal currentPrice,
        BigDecimal estimatedValue,
        BigDecimal estimatedProfitLoss
) {
}
