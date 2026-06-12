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
        name = "positions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_positions_user_market_outcome",
                columnNames = {"user_id", "market_id", "outcome_id"}
        )
)
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "outcome_id", nullable = false)
    private Outcome outcome;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal units = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal averageEntryPrice = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public BigDecimal getUnits() {
        return units;
    }

    public void setUnits(BigDecimal units) {
        this.units = units;
    }

    public BigDecimal getAverageEntryPrice() {
        return averageEntryPrice;
    }

    public void setAverageEntryPrice(BigDecimal averageEntryPrice) {
        this.averageEntryPrice = averageEntryPrice;
    }
}
