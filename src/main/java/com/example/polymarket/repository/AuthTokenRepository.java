package com.example.polymarket.repository;

import com.example.polymarket.domain.AuthToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);

    @Query("""
            select t from AuthToken t
            join fetch t.user
            where t.tokenHash = :tokenHash and t.expiresAt > :now
            """)
    Optional<AuthToken> findActiveToken(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

    void deleteByExpiresAtBefore(Instant now);
}
