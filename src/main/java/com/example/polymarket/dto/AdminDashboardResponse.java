package com.example.polymarket.dto;

import java.math.BigDecimal;

public record AdminDashboardResponse(
        long users,
        long openMarkets,
        long closedMarkets,
        long marketsWaitingForResolution,
        BigDecimal totalTradeVolume
) {
}
