package com.example.polymarket.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "markets")
public class Market {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 240)
    private String question;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false)
    private Instant tradingCloseAt;

    @Column(nullable = false)
    private Instant resolutionAt;

    @Column(nullable = false, length = 500)
    private String resolutionSource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MarketStatus status = MarketStatus.OPEN;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal liquidity = new BigDecimal("1000");

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_outcome_id")
    private Outcome resolvedOutcome;

    @OneToMany(mappedBy = "market", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Outcome> outcomes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Instant getTradingCloseAt() {
        return tradingCloseAt;
    }

    public void setTradingCloseAt(Instant tradingCloseAt) {
        this.tradingCloseAt = tradingCloseAt;
    }

    public Instant getResolutionAt() {
        return resolutionAt;
    }

    public void setResolutionAt(Instant resolutionAt) {
        this.resolutionAt = resolutionAt;
    }

    public String getResolutionSource() {
        return resolutionSource;
    }

    public void setResolutionSource(String resolutionSource) {
        this.resolutionSource = resolutionSource;
    }

    public MarketStatus getStatus() {
        return status;
    }

    public void setStatus(MarketStatus status) {
        this.status = status;
    }

    public BigDecimal getLiquidity() {
        return liquidity;
    }

    public void setLiquidity(BigDecimal liquidity) {
        this.liquidity = liquidity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Outcome getResolvedOutcome() {
        return resolvedOutcome;
    }

    public void setResolvedOutcome(Outcome resolvedOutcome) {
        this.resolvedOutcome = resolvedOutcome;
    }

    public List<Outcome> getOutcomes() {
        return outcomes;
    }

    public void addOutcome(Outcome outcome) {
        outcome.setMarket(this);
        outcomes.add(outcome);
    }
}
