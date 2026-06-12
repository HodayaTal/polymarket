package com.example.polymarket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "market_resolutions")
public class MarketResolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_id", nullable = false, unique = true)
    private Market market;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "winning_outcome_id", nullable = false)
    private Outcome winningOutcome;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resolved_by_user_id", nullable = false)
    private AppUser resolvedBy;

    @Column(nullable = false, length = 1000)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant resolvedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public Outcome getWinningOutcome() {
        return winningOutcome;
    }

    public void setWinningOutcome(Outcome winningOutcome) {
        this.winningOutcome = winningOutcome;
    }

    public AppUser getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(AppUser resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
