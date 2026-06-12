package com.example.polymarket.repository;

import com.example.polymarket.domain.PriceHistory;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    @EntityGraph(attributePaths = {"outcome"})
    List<PriceHistory> findTop200ByMarketIdOrderByRecordedAtAsc(Long marketId);
}
