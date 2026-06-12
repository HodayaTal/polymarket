package com.example.polymarket.dto;

import com.example.polymarket.domain.WalletMovementType;
import java.math.BigDecimal;
import java.time.Instant;

public record WalletMovementResponse(
        Long id,
        WalletMovementType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        Instant createdAt
) {
}
