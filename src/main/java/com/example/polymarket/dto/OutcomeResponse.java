package com.example.polymarket.dto;

import java.math.BigDecimal;

public record OutcomeResponse(
        Long id,
        String label,
        BigDecimal currentPrice,
        BigDecimal demandUnits
) {
}
