package com.example.polymarket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveMarketRequest(
        @NotNull Long winningOutcomeId,
        @Size(max = 1000) String notes
) {
}
