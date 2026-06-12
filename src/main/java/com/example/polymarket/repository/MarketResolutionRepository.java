package com.example.polymarket.repository;

import com.example.polymarket.domain.MarketResolution;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketResolutionRepository extends JpaRepository<MarketResolution, Long> {

    Optional<MarketResolution> findByMarketId(Long marketId);
}
