package com.example.springbootecommerce.auth.repository;

import com.example.springbootecommerce.auth.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    Optional<TokenBlacklist> findByToken(String token);

    void deleteByExpiryDateBefore(java.time.Instant now);
}
