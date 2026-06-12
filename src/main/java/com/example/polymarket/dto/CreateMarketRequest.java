package com.example.polymarket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CreateMarketRequest(
        @NotBlank @Size(max = 240) String question,
        @NotBlank @Size(max = 4000) String description,
        @NotBlank @Size(max = 80) String category,
        @Future Instant tradingCloseAt,
        @Future Instant resolutionAt,
        @NotBlank @Size(max = 500) String resolutionSource,
        @DecimalMin(value = "1.00") BigDecimal liquidity,
        List<@NotBlank @Size(max = 80) String> outcomes
) {
}
