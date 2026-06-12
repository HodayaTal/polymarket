package com.example.polymarket.repository;

import com.example.polymarket.domain.Outcome;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutcomeRepository extends JpaRepository<Outcome, Long> {

    Optional<Outcome> findByIdAndMarketId(Long id, Long marketId);
}
