package com.example.polymarket.repository;

import com.example.polymarket.domain.Market;
import com.example.polymarket.domain.MarketStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketRepository extends JpaRepository<Market, Long> {

    @EntityGraph(attributePaths = "outcomes")
    List<Market> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "outcomes")
    List<Market> findByStatusOrderByCreatedAtDesc(MarketStatus status);

    @EntityGraph(attributePaths = "outcomes")
    Optional<Market> findWithOutcomesById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct m from Market m left join fetch m.outcomes where m.id = :id")
    Optional<Market> findByIdForUpdate(@Param("id") Long id);

    long countByStatus(MarketStatus status);

    long countByStatusAndResolutionAtBefore(MarketStatus status, Instant now);
}
