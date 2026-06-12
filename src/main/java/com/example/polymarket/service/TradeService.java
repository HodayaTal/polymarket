package com.example.polymarket.service;

import com.example.polymarket.domain.AppUser;
import com.example.polymarket.domain.Market;
import com.example.polymarket.domain.MarketStatus;
import com.example.polymarket.domain.Outcome;
import com.example.polymarket.domain.Position;
import com.example.polymarket.domain.Trade;
import com.example.polymarket.domain.TradeSide;
import com.example.polymarket.domain.WalletMovement;
import com.example.polymarket.domain.WalletMovementType;
import com.example.polymarket.dto.TradeRequest;
import com.example.polymarket.dto.TradeResponse;
import com.example.polymarket.exception.ApiException;
import com.example.polymarket.repository.AppUserRepository;
import com.example.polymarket.repository.MarketRepository;
import com.example.polymarket.repository.PositionRepository;
import com.example.polymarket.repository.TradeRepository;
import com.example.polymarket.repository.WalletMovementRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeService {

    private final AppUserRepository appUserRepository;
    private final MarketRepository marketRepository;
    private final PositionRepository positionRepository;
    private final TradeRepository tradeRepository;
    private final WalletMovementRepository walletMovementRepository;
    private final PricingService pricingService;
    private final ApiMapper mapper;
    private final LiveEventService liveEventService;

    public TradeService(
            AppUserRepository appUserRepository,
            MarketRepository marketRepository,
            PositionRepository positionRepository,
            TradeRepository tradeRepository,
            WalletMovementRepository walletMovementRepository,
            PricingService pricingService,
            ApiMapper mapper,
            LiveEventService liveEventService
    ) {
        this.appUserRepository = appUserRepository;
        this.marketRepository = marketRepository;
        this.positionRepository = positionRepository;
        this.tradeRepository = tradeRepository;
        this.walletMovementRepository = walletMovementRepository;
        this.pricingService = pricingService;
        this.mapper = mapper;
        this.liveEventService = liveEventService;
    }

    @Transactional
    public TradeResponse execute(AppUser caller, TradeRequest request) {
        Market market = marketRepository.findByIdForUpdate(request.marketId())
                .orElseThrow(() -> ApiException.notFound("Market not found."));
        ensureTradable(market);

        Outcome outcome = market.getOutcomes().stream()
                .filter(candidate -> candidate.getId().equals(request.outcomeId()))
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest("Outcome does not belong to this market."));

        AppUser user = appUserRepository.findByIdForUpdate(caller.getId())
                .orElseThrow(() -> ApiException.notFound("User not found."));
        Position position = positionRepository.findForUpdate(user.getId(), market.getId(), outcome.getId())
                .orElseGet(() -> newPosition(user, market, outcome));

        BigDecimal units = MoneyMath.units(request.units());
        PriceQuote quote = pricingService.quote(market, outcome, request.side(), units);

        if (request.side() == TradeSide.BUY) {
            applyBuy(user, position, units, quote);
        } else {
            applySell(user, position, units, quote);
        }

        pricingService.applyDemandAndRecord(market, outcome, request.side(), units);
        Trade trade = saveTrade(user, market, outcome, request.side(), units, quote);
        saveWalletMovement(user, trade, request.side(), quote.total());

        TradeResponse response = mapper.toTradeResponse(trade);
        liveEventService.publish("trade-created", response);
        liveEventService.publish("market-updated", mapper.toMarketResponse(market));
        return response;
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> recentTrades() {
        return tradeRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toTradeResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> recentTradesForMarket(Long marketId) {
        return tradeRepository.findRecentByMarketId(marketId, PageRequest.of(0, 20))
                .stream()
                .map(mapper::toTradeResponse)
                .toList();
    }

    private void ensureTradable(Market market) {
        if (market.getStatus() != MarketStatus.OPEN) {
            throw ApiException.badRequest("Market is not open for trading.");
        }
        if (Instant.now().isAfter(market.getTradingCloseAt())) {
            market.setStatus(MarketStatus.CLOSED);
            throw ApiException.badRequest("Market trading close time has passed.");
        }
    }

    private Position newPosition(AppUser user, Market market, Outcome outcome) {
        Position position = new Position();
        position.setUser(user);
        position.setMarket(market);
        position.setOutcome(outcome);
        return position;
    }

    private void applyBuy(AppUser user, Position position, BigDecimal units, PriceQuote quote) {
        if (user.getWalletBalance().compareTo(quote.total()) < 0) {
            throw ApiException.badRequest("Insufficient virtual points.");
        }
        BigDecimal oldUnits = position.getUnits();
        BigDecimal newUnits = oldUnits.add(units);
        BigDecimal oldCost = oldUnits.multiply(position.getAverageEntryPrice());
        BigDecimal newCost = units.multiply(quote.averagePrice());
        position.setUnits(MoneyMath.units(newUnits));
        position.setAverageEntryPrice(MoneyMath.price(
                oldCost.add(newCost).divide(newUnits, 8, RoundingMode.HALF_UP)
        ));
        user.setWalletBalance(MoneyMath.money(user.getWalletBalance().subtract(quote.total())));
        positionRepository.save(position);
    }

    private void applySell(AppUser user, Position position, BigDecimal units, PriceQuote quote) {
        if (position.getUnits().compareTo(units) < 0) {
            throw ApiException.badRequest("Cannot sell more units than the open position holds.");
        }
        BigDecimal remaining = position.getUnits().subtract(units);
        position.setUnits(MoneyMath.units(remaining));
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            position.setAverageEntryPrice(BigDecimal.ZERO);
        }
        user.setWalletBalance(MoneyMath.money(user.getWalletBalance().add(quote.total())));
        positionRepository.save(position);
    }

    private Trade saveTrade(AppUser user, Market market, Outcome outcome, TradeSide side, BigDecimal units, PriceQuote quote) {
        Trade trade = new Trade();
        trade.setUser(user);
        trade.setMarket(market);
        trade.setOutcome(outcome);
        trade.setSide(side);
        trade.setUnits(units);
        trade.setPrice(quote.averagePrice());
        trade.setTotal(quote.total());
        trade.setBalanceAfter(user.getWalletBalance());
        return tradeRepository.save(trade);
    }

    private void saveWalletMovement(AppUser user, Trade trade, TradeSide side, BigDecimal total) {
        WalletMovement movement = new WalletMovement();
        movement.setUser(user);
        movement.setTrade(trade);
        movement.setType(side == TradeSide.BUY ? WalletMovementType.BUY : WalletMovementType.SELL);
        movement.setAmount(side == TradeSide.BUY ? total.negate() : total);
        movement.setBalanceAfter(user.getWalletBalance());
        movement.setDescription(side + " " + trade.getUnits() + " units of " + trade.getOutcome().getLabel());
        walletMovementRepository.save(movement);
    }
}
