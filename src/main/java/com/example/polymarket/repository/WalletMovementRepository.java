package com.example.polymarket.repository;

import com.example.polymarket.domain.WalletMovement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletMovementRepository extends JpaRepository<WalletMovement, Long> {

    List<WalletMovement> findByUserIdOrderByCreatedAtDesc(Long userId);
}
