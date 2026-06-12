package com.example.polymarket.service;

import com.example.polymarket.domain.AppUser;
import com.example.polymarket.dto.AdminDashboardResponse;
import com.example.polymarket.dto.DashboardResponse;
import com.example.polymarket.dto.PositionResponse;
import com.example.polymarket.dto.UserResponse;
import com.example.polymarket.domain.MarketStatus;
import com.example.polymarket.repository.AppUserRepository;
import com.example.polymarket.repository.MarketRepository;
import com.example.polymarket.repository.PositionRepository;
import com.example.polymarket.repository.TradeRepository;
import com.example.polymarket.repository.WalletMovementRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final AppUserRepository appUserRepository;
    private final PositionRepository positionRepository;
    private final TradeRepository tradeRepository;
    private final WalletMovementRepository walletMovementRepository;
    private final MarketRepository marketRepository;
    private final ApiMapper mapper;

    public DashboardService(
            AppUserRepository appUserRepository,
            PositionRepository positionRepository,
            TradeRepository tradeRepository,
            WalletMovementRepository walletMovementRepository,
            MarketRepository marketRepository,
            ApiMapper mapper
    ) {
        this.appUserRepository = appUserRepository;
        this.positionRepository = positionRepository;
        this.tradeRepository = tradeRepository;
        this.walletMovementRepository = walletMovementRepository;
        this.marketRepository = marketRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public DashboardResponse userDashboard(AppUser user) {
        AppUser freshUser = appUserRepository.findById(user.getId())
                .orElseThrow(() -> com.example.polymarket.exception.ApiException.notFound("User not found."));
        List<PositionResponse> positions = positionRepository.findByUserIdOrderByIdDesc(user.getId())
                .stream()
                .filter(position -> position.getUnits().compareTo(BigDecimal.ZERO) > 0)
                .map(mapper::toPositionResponse)
                .toList();
        BigDecimal estimatedValue = positions.stream()
                .map(PositionResponse::estimatedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal estimatedProfitLoss = positions.stream()
                .map(PositionResponse::estimatedProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardResponse(
                mapper.toUserResponse(freshUser),
                MoneyMath.money(estimatedValue),
                MoneyMath.money(estimatedProfitLoss),
                positions,
                tradeRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                        .map(mapper::toTradeResponse)
                        .toList(),
                walletMovementRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                        .map(mapper::toWalletMovementResponse)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponse> leaderboard() {
        return appUserRepository.findLeaderboard(PageRequest.of(0, 20))
                .stream()
                .map(mapper::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse adminDashboard() {
        return new AdminDashboardResponse(
                appUserRepository.count(),
                marketRepository.countByStatus(MarketStatus.OPEN),
                marketRepository.countByStatus(MarketStatus.CLOSED),
                marketRepository.countByStatusAndResolutionAtBefore(MarketStatus.CLOSED, Instant.now()),
                MoneyMath.money(tradeRepository.totalVolume())
        );
    }
}
