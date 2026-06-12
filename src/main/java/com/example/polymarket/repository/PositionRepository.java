package com.example.polymarket.repository;

import com.example.polymarket.domain.Position;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PositionRepository extends JpaRepository<Position, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p from Position p
            where p.user.id = :userId
              and p.market.id = :marketId
              and p.outcome.id = :outcomeId
            """)
    Optional<Position> findForUpdate(
            @Param("userId") Long userId,
            @Param("marketId") Long marketId,
            @Param("outcomeId") Long outcomeId
    );

    @EntityGraph(attributePaths = {"market", "outcome"})
    List<Position> findByUserIdOrderByIdDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "outcome"})
    List<Position> findByMarketId(Long marketId);
}
