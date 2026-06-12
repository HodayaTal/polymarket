package com.example.polymarket.service;

import com.example.polymarket.domain.AppUser;
import com.example.polymarket.domain.Market;
import com.example.polymarket.domain.Outcome;
import com.example.polymarket.domain.Position;
import com.example.polymarket.domain.PriceHistory;
import com.example.polymarket.domain.Trade;
import com.example.polymarket.domain.WalletMovement;
import com.example.polymarket.dto.MarketResponse;
import com.example.polymarket.dto.OutcomeResponse;
import com.example.polymarket.dto.PositionResponse;
import com.example.polymarket.dto.PricePointResponse;
import com.example.polymarket.dto.TradeResponse;
import com.example.polymarket.dto.UserResponse;
import com.example.polymarket.dto.WalletMovementResponse;
import com.example.polymarket.repository.TradeRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import org.springframework.stereotype.Service;

@Service
public class ApiMapper {

    private final TradeRepository tradeRepository;

    public ApiMapper(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public UserResponse toUserResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getWalletBalance(),
                user.getReputationScore(),
                user.getCorrectPredictions(),
                user.getResolvedPredictions(),
                user.getCreatedAt()
        );
    }

    public MarketResponse toMarketResponse(Market market) {
        Long resolvedOutcomeId = market.getResolvedOutcome() == null ? null : market.getResolvedOutcome().getId();
        return new MarketResponse(
                market.getId(),
                market.getQuestion(),
                market.getDescription(),
                market.getCategory(),
                market.getTradingCloseAt(),
                market.getResolutionAt(),
                market.getResolutionSource(),
                market.getStatus(),
                market.getLiquidity(),
                resolvedOutcomeId,
                market.getOutcomes().stream()
                        .sorted(Comparator.comparing(Outcome::getId, Comparator.nullsLast(Long::compareTo)))
                        .map(this::toOutcomeResponse)
                        .toList(),
                tradeRepository.countByMarketId(market.getId()),
                market.getCreatedAt()
        );
    }

    public OutcomeResponse toOutcomeResponse(Outcome outcome) {
        return new OutcomeResponse(
                outcome.getId(),
                outcome.getLabel(),
                outcome.getCurrentPrice(),
                outcome.getDemandUnits()
        );
    }

    public TradeResponse toTradeResponse(Trade trade) {
        return new TradeResponse(
                trade.getId(),
                trade.getUser().getId(),
                trade.getUser().getUsername(),
                trade.getMarket().getId(),
                trade.getMarket().getQuestion(),
                trade.getOutcome().getId(),
                trade.getOutcome().getLabel(),
                trade.getSide(),
                trade.getUnits(),
                trade.getPrice(),
                trade.getTotal(),
                trade.getBalanceAfter(),
                trade.getCreatedAt()
        );
    }

    public PositionResponse toPositionResponse(Position position) {
        BigDecimal estimatedValue = MoneyMath.money(position.getUnits().multiply(position.getOutcome().getCurrentPrice()));
        BigDecimal entryValue = MoneyMath.money(position.getUnits().multiply(position.getAverageEntryPrice()));
        return new PositionResponse(
                position.getMarket().getId(),
                position.getMarket().getQuestion(),
                position.getOutcome().getId(),
                position.getOutcome().getLabel(),
                position.getUnits(),
                position.getAverageEntryPrice(),
                position.getOutcome().getCurrentPrice(),
                estimatedValue,
                MoneyMath.money(estimatedValue.subtract(entryValue))
        );
    }

    public WalletMovementResponse toWalletMovementResponse(WalletMovement movement) {
        return new WalletMovementResponse(
                movement.getId(),
                movement.getType(),
                movement.getAmount(),
                movement.getBalanceAfter(),
                movement.getDescription(),
                movement.getCreatedAt()
        );
    }

    public PricePointResponse toPricePointResponse(PriceHistory point) {
        return new PricePointResponse(
                point.getOutcome().getId(),
                point.getOutcome().getLabel(),
                point.getPrice(),
                point.getRecordedAt()
        );
    }
}
