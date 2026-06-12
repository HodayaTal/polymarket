package com.example.polymarket.dto;

import com.example.polymarket.domain.MarketStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record MarketResponse(
        Long id,
        String question,
        String description,
        String category,
        Instant tradingCloseAt,
        Instant resolutionAt,
        String resolutionSource,
        MarketStatus status,
        BigDecimal liquidity,
        Long resolvedOutcomeId,
        List<OutcomeResponse> outcomes,
        long tradeCount,
        Instant createdAt
) {
}
