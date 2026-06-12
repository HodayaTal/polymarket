package com.example.polymarket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(
        name = "outcomes",
        uniqueConstraints = @UniqueConstraint(name = "uk_outcomes_market_label", columnNames = {"market_id", "label"})
)
public class Outcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @Column(nullable = false, length = 80)
    private String label;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal currentPrice = new BigDecimal("0.5000");

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal demandUnits = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getDemandUnits() {
        return demandUnits;
    }

    public void setDemandUnits(BigDecimal demandUnits) {
        this.demandUnits = demandUnits;
    }
}
