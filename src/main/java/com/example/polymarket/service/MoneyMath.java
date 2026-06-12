package com.example.polymarket.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyMath {

    private MoneyMath() {
    }

    public static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal units(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal price(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
