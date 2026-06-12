package com.example.polymarket.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        UserResponse user,
        BigDecimal estimatedPortfolioValue,
        BigDecimal estimatedProfitLoss,
        List<PositionResponse> positions,
        List<TradeResponse> trades,
        List<WalletMovementResponse> walletMovements
) {
}
