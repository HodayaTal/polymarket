package com.example.polymarket.repository;

import com.example.polymarket.domain.Trade;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    @EntityGraph(attributePaths = {"user", "market", "outcome"})
    List<Trade> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "market", "outcome"})
    List<Trade> findByMarketIdOrderByCreatedAtDesc(Long marketId);

    @EntityGraph(attributePaths = {"user", "market", "outcome"})
    List<Trade> findTop20ByOrderByCreatedAtDesc();

    long countByMarketId(Long marketId);

    @Query("select coalesce(sum(t.total), 0) from Trade t")
    BigDecimal totalVolume();

    @EntityGraph(attributePaths = {"user", "market", "outcome"})
    @Query("select t from Trade t where t.market.id = :marketId order by t.createdAt desc")
    List<Trade> findRecentByMarketId(@Param("marketId") Long marketId, Pageable pageable);
}
