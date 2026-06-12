package com.example.polymarket.service;

import com.example.polymarket.domain.AppUser;
import com.example.polymarket.domain.Market;
import com.example.polymarket.domain.MarketResolution;
import com.example.polymarket.domain.MarketStatus;
import com.example.polymarket.domain.Outcome;
import com.example.polymarket.domain.Position;
import com.example.polymarket.domain.WalletMovement;
import com.example.polymarket.domain.WalletMovementType;
import com.example.polymarket.dto.CreateMarketRequest;
import com.example.polymarket.dto.MarketResponse;
import com.example.polymarket.dto.PricePointResponse;
import com.example.polymarket.dto.ResolveMarketRequest;
import com.example.polymarket.exception.ApiException;
import com.example.polymarket.repository.AppUserRepository;
import com.example.polymarket.repository.MarketRepository;
import com.example.polymarket.repository.MarketResolutionRepository;
import com.example.polymarket.repository.PositionRepository;
import com.example.polymarket.repository.PriceHistoryRepository;
import com.example.polymarket.repository.WalletMovementRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketService {

    private final MarketRepository marketRepository;
    private final MarketResolutionRepository marketResolutionRepository;
    private final PositionRepository positionRepository;
    private final AppUserRepository appUserRepository;
    private final WalletMovementRepository walletMovementRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PricingService pricingService;
    private final ApiMapper mapper;
    private final LiveEventService liveEventService;

    public MarketService(
            MarketRepository marketRepository,
            MarketResolutionRepository marketResolutionRepository,
            PositionRepository positionRepository,
            AppUserRepository appUserRepository,
            WalletMovementRepository walletMovementRepository,
            PriceHistoryRepository priceHistoryRepository,
            PricingService pricingService,
            ApiMapper mapper,
            LiveEventService liveEventService
    ) {
        this.marketRepository = marketRepository;
        this.marketResolutionRepository = marketResolutionRepository;
        this.positionRepository = positionRepository;
        this.appUserRepository = appUserRepository;
        this.walletMovementRepository = walletMovementRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.pricingService = pricingService;
        this.mapper = mapper;
        this.liveEventService = liveEventService;
    }

    @Transactional(readOnly = true)
    public List<MarketResponse> list(String status) {
        List<Market> markets;
        if (status == null || status.isBlank()) {
            markets = marketRepository.findAllByOrderByCreatedAtDesc();
        } else {
            markets = marketRepository.findByStatusOrderByCreatedAtDesc(
                    MarketStatus.valueOf(status.trim().toUpperCase(Locale.ROOT))
            );
        }
        return markets.stream().map(mapper::toMarketResponse).toList();
    }

    @Transactional(readOnly = true)
    public MarketResponse get(Long id) {
        return mapper.toMarketResponse(findMarketWithOutcomes(id));
    }

    @Transactional
    public MarketResponse create(CreateMarketRequest request) {
        if (!request.resolutionAt().isAfter(request.tradingCloseAt())) {
            throw ApiException.badRequest("Resolution date must be after the trading close date.");
        }

        List<String> labels = normalizeOutcomeLabels(request.outcomes());
        Market market = new Market();
        market.setQuestion(request.question().trim());
        market.setDescription(request.description().trim());
        market.setCategory(request.category().trim());
        market.setTradingCloseAt(request.tradingCloseAt());
        market.setResolutionAt(request.resolutionAt());
        market.setResolutionSource(request.resolutionSource().trim());
        if (request.liquidity() != null) {
            market.setLiquidity(MoneyMath.money(request.liquidity()));
        }

        for (String label : labels) {
            Outcome outcome = new Outcome();
            outcome.setLabel(label);
            market.addOutcome(outcome);
        }

        Market saved = marketRepository.save(market);
        pricingService.repriceAndRecord(saved);
        MarketResponse response = mapper.toMarketResponse(saved);
        liveEventService.publish("market-created", response);
        return response;
    }

    @Transactional
    public MarketResponse close(Long marketId) {
        Market market = marketRepository.findByIdForUpdate(marketId)
                .orElseThrow(() -> ApiException.notFound("Market not found."));
        if (market.getStatus() != MarketStatus.OPEN) {
            throw ApiException.badRequest("Only open markets can be closed.");
        }
        market.setStatus(MarketStatus.CLOSED);
        MarketResponse response = mapper.toMarketResponse(market);
        liveEventService.publish("market-updated", response);
        return response;
    }

    @Transactional
    public MarketResponse cancel(Long marketId) {
        Market market = marketRepository.findByIdForUpdate(marketId)
                .orElseThrow(() -> ApiException.notFound("Market not found."));
        if (market.getStatus() == MarketStatus.RESOLVED) {
            throw ApiException.badRequest("Resolved markets cannot be cancelled.");
        }
        market.setStatus(MarketStatus.CANCELLED);
        MarketResponse response = mapper.toMarketResponse(market);
        liveEventService.publish("market-updated", response);
        return response;
    }

    @Transactional
    public MarketResponse resolve(Long marketId, ResolveMarketRequest request, AppUser admin) {
        Market market = marketRepository.findByIdForUpdate(marketId)
                .orElseThrow(() -> ApiException.notFound("Market not found."));
        if (market.getStatus() == MarketStatus.RESOLVED || market.getStatus() == MarketStatus.CANCELLED) {
            throw ApiException.badRequest("Market cannot be resolved from its current status.");
        }
        if (market.getStatus() == MarketStatus.OPEN && Instant.now().isBefore(market.getTradingCloseAt())) {
            throw ApiException.badRequest("Market trading is still open.");
        }

        Outcome winningOutcome = market.getOutcomes().stream()
                .filter(outcome -> outcome.getId().equals(request.winningOutcomeId()))
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest("Winning outcome does not belong to this market."));

        market.setStatus(MarketStatus.RESOLVED);
        market.setResolvedOutcome(winningOutcome);

        MarketResolution resolution = new MarketResolution();
        resolution.setMarket(market);
        resolution.setWinningOutcome(winningOutcome);
        resolution.setResolvedBy(admin);
        resolution.setNotes(request.notes() == null || request.notes().isBlank() ? "Resolved by admin." : request.notes().trim());
        marketResolutionRepository.save(resolution);

        settlePositions(market, winningOutcome);

        MarketResponse response = mapper.toMarketResponse(market);
        liveEventService.publish("market-resolved", response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<PricePointResponse> history(Long marketId) {
        findMarketWithOutcomes(marketId);
        return priceHistoryRepository.findTop200ByMarketIdOrderByRecordedAtAsc(marketId)
                .stream()
                .map(mapper::toPricePointResponse)
                .toList();
    }

    private void settlePositions(Market market, Outcome winningOutcome) {
        List<Position> positions = positionRepository.findByMarketId(market.getId());
        Map<Long, UserSettlement> settlements = new LinkedHashMap<>();
        for (Position position : positions) {
            if (position.getUnits().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            UserSettlement settlement = settlements.computeIfAbsent(
                    position.getUser().getId(),
                    userId -> new UserSettlement(position.getUser().getId())
            );
            if (position.getOutcome().getId().equals(winningOutcome.getId())) {
                settlement.winningUnits = settlement.winningUnits.add(position.getUnits());
            } else {
                settlement.losingUnits = settlement.losingUnits.add(position.getUnits());
            }
            position.setUnits(BigDecimal.ZERO);
        }

        for (UserSettlement settlement : settlements.values()) {
            AppUser user = appUserRepository.findByIdForUpdate(settlement.userId)
                    .orElseThrow(() -> ApiException.notFound("User not found during settlement."));
            boolean correct = settlement.winningUnits.compareTo(settlement.losingUnits) > 0;
            user.setResolvedPredictions(user.getResolvedPredictions() + 1);
            if (correct) {
                user.setCorrectPredictions(user.getCorrectPredictions() + 1);
            }
            BigDecimal reputation = BigDecimal.valueOf(user.getCorrectPredictions())
                    .multiply(new BigDecimal("100"))
                    .divide(BigDecimal.valueOf(user.getResolvedPredictions()), 4, RoundingMode.HALF_UP);
            user.setReputationScore(reputation);

            BigDecimal payout = MoneyMath.money(settlement.winningUnits);
            if (payout.compareTo(BigDecimal.ZERO) > 0) {
                user.setWalletBalance(MoneyMath.money(user.getWalletBalance().add(payout)));
                WalletMovement movement = new WalletMovement();
                movement.setUser(user);
                movement.setType(WalletMovementType.SETTLEMENT);
                movement.setAmount(payout);
                movement.setBalanceAfter(user.getWalletBalance());
                movement.setDescription("Settlement payout for market: " + market.getQuestion());
                walletMovementRepository.save(movement);
            }
        }
    }

    private List<String> normalizeOutcomeLabels(List<String> requestedOutcomes) {
        List<String> labels = requestedOutcomes == null || requestedOutcomes.isEmpty()
                ? List.of("Yes", "No")
                : requestedOutcomes;
        if (labels.size() != 2) {
            throw ApiException.badRequest("This implementation supports exactly two outcomes.");
        }
        List<String> normalized = new ArrayList<>();
        for (String label : labels) {
            String trimmed = label.trim();
            if (trimmed.isBlank()) {
                throw ApiException.badRequest("Outcome labels cannot be blank.");
            }
            if (normalized.stream().anyMatch(existing -> existing.equalsIgnoreCase(trimmed))) {
                throw ApiException.badRequest("Outcome labels must be unique.");
            }
            normalized.add(trimmed);
        }
        return normalized;
    }

    private Market findMarketWithOutcomes(Long id) {
        return marketRepository.findWithOutcomesById(id)
                .orElseThrow(() -> ApiException.notFound("Market not found."));
    }

    private static class UserSettlement {
        private final Long userId;
        private BigDecimal winningUnits = BigDecimal.ZERO;
        private BigDecimal losingUnits = BigDecimal.ZERO;

        private UserSettlement(Long userId) {
            this.userId = userId;
        }
    }
}
