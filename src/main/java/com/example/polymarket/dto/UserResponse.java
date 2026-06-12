package com.example.polymarket.dto;

import com.example.polymarket.domain.UserRole;
import java.math.BigDecimal;
import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String email,
        UserRole role,
        BigDecimal walletBalance,
        BigDecimal reputationScore,
        int correctPredictions,
        int resolvedPredictions,
        Instant createdAt
) {
}
