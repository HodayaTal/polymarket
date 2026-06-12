package com.example.polymarket.dto;

import com.example.polymarket.domain.TradeSide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TradeRequest(
        @NotNull Long marketId,
        @NotNull Long outcomeId,
        @NotNull TradeSide side,
        @NotNull @DecimalMin(value = "0.01") BigDecimal units
) {
}
