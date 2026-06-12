package com.example.polymarket.service;

import java.math.BigDecimal;

public record PriceQuote(
        BigDecimal beforePrice,
        BigDecimal afterPrice,
        BigDecimal averagePrice,
        BigDecimal total
) {
}
