package com.example.polymarket.service;

import com.example.polymarket.domain.Market;
import com.example.polymarket.domain.Outcome;
import com.example.polymarket.domain.PriceHistory;
import com.example.polymarket.domain.TradeSide;
import com.example.polymarket.exception.ApiException;
import com.example.polymarket.repository.PriceHistoryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private static final BigDecimal MIN_PRICE = new BigDecimal("0.0100");
    private static final BigDecimal MAX_PRICE = new BigDecimal("0.9900");

    private final PriceHistoryRepository priceHistoryRepository;

    public PricingService(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
    }

    public PriceQuote quote(Market market, Outcome selectedOutcome, TradeSide side, BigDecimal rawUnits) {
        BigDecimal units = MoneyMath.units(rawUnits);
        if (units.compareTo(BigDecimal.ZERO) <= 0) {
            throw ApiException.badRequest("Units must be greater than zero.");
        }

        List<Outcome> outcomes = orderedBinaryOutcomes(market);
        BigDecimal before = selectedOutcome.getCurrentPrice();

        Outcome first = outcomes.get(0);
        Outcome second = outcomes.get(1);
        BigDecimal firstDemand = first.getDemandUnits();
        BigDecimal secondDemand = second.getDemandUnits();
        BigDecimal signedUnits = side == TradeSide.BUY ? units : units.negate();

        if (first.getId().equals(selectedOutcome.getId())) {
            firstDemand = firstDemand.add(signedUnits);
        } else {
            secondDemand = secondDemand.add(signedUnits);
        }

        BigDecimal firstAfterPrice = priceForFirstOutcome(firstDemand, secondDemand, market.getLiquidity());
        BigDecimal after = first.getId().equals(selectedOutcome.getId())
                ? firstAfterPrice
                : MoneyMath.price(BigDecimal.ONE.subtract(firstAfterPrice));

        BigDecimal average = MoneyMath.price(before.add(after).divide(new BigDecimal("2"), 8, RoundingMode.HALF_UP));
        BigDecimal total = MoneyMath.money(average.multiply(units));
        return new PriceQuote(before, after, average, total);
    }

    public void applyDemandAndRecord(Market market, Outcome selectedOutcome, TradeSide side, BigDecimal rawUnits) {
        BigDecimal signedUnits = side == TradeSide.BUY ? MoneyMath.units(rawUnits) : MoneyMath.units(rawUnits).negate();
        selectedOutcome.setDemandUnits(MoneyMath.units(selectedOutcome.getDemandUnits().add(signedUnits)));
        repriceAndRecord(market);
    }

    public void repriceAndRecord(Market market) {
        List<Outcome> outcomes = orderedBinaryOutcomes(market);
        Outcome first = outcomes.get(0);
        Outcome second = outcomes.get(1);
        BigDecimal firstPrice = priceForFirstOutcome(first.getDemandUnits(), second.getDemandUnits(), market.getLiquidity());
        first.setCurrentPrice(firstPrice);
        second.setCurrentPrice(MoneyMath.price(BigDecimal.ONE.subtract(firstPrice)));
        recordPrice(market, first);
        recordPrice(market, second);
    }

    private void recordPrice(Market market, Outcome outcome) {
        PriceHistory history = new PriceHistory();
        history.setMarket(market);
        history.setOutcome(outcome);
        history.setPrice(outcome.getCurrentPrice());
        priceHistoryRepository.save(history);
    }

    private BigDecimal priceForFirstOutcome(BigDecimal firstDemand, BigDecimal secondDemand, BigDecimal liquidity) {
        double safeLiquidity = Math.max(1.0, liquidity.doubleValue());
        double imbalance = firstDemand.subtract(secondDemand).doubleValue();
        double sigmoid = 1.0 / (1.0 + Math.exp(-(imbalance / safeLiquidity)));
        BigDecimal price = MoneyMath.price(BigDecimal.valueOf(sigmoid));
        if (price.compareTo(MIN_PRICE) < 0) {
            return MIN_PRICE;
        }
        if (price.compareTo(MAX_PRICE) > 0) {
            return MAX_PRICE;
        }
        return price;
    }

    private List<Outcome> orderedBinaryOutcomes(Market market) {
        List<Outcome> outcomes = market.getOutcomes().stream()
                .sorted(Comparator.comparing(Outcome::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
        if (outcomes.size() != 2) {
            throw ApiException.badRequest("This pricing engine supports exactly two outcomes.");
        }
        return outcomes;
    }
}
