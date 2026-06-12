package com.example.polymarket.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "polymarket")
public class PolymarketProperties {

    private BigDecimal initialWalletPoints = new BigDecimal("10000");
    private String adminRegistrationCode = "";

    public BigDecimal getInitialWalletPoints() {
        return initialWalletPoints;
    }

    public void setInitialWalletPoints(BigDecimal initialWalletPoints) {
        this.initialWalletPoints = initialWalletPoints;
    }

    public String getAdminRegistrationCode() {
        return adminRegistrationCode;
    }

    public void setAdminRegistrationCode(String adminRegistrationCode) {
        this.adminRegistrationCode = adminRegistrationCode;
    }
}
