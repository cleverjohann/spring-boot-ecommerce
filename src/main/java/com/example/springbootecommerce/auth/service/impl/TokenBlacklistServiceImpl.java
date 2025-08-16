package com.example.springbootecommerce.auth.service.impl;

import com.example.springbootecommerce.auth.entity.TokenBlacklist;
import com.example.springbootecommerce.auth.repository.TokenBlacklistRepository;
import com.example.springbootecommerce.auth.service.TokenBlacklistService;
import com.example.springbootecommerce.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void blacklistToken(String token) {
        Instant expiryDate = jwtService.extractExpiration(token).toInstant();
        TokenBlacklist tokenBlacklist = TokenBlacklist.builder()
                .token(token)
                .expiryDate(expiryDate)
                .build();
        tokenBlacklistRepository.save(tokenBlacklist);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.findByToken(token).isPresent();
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        tokenBlacklistRepository.deleteByExpiryDateBefore(Instant.now());
    }
}
