package com.example.springbootecommerce.auth.task;

import com.example.springbootecommerce.auth.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistCleanupTask {

    private final TokenBlacklistService tokenBlacklistService;

    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    public void cleanupExpiredTokens() {
        log.info("Running cleanup of expired tokens from blacklist...");
        tokenBlacklistService.cleanupExpiredTokens();
        log.info("Cleanup of expired tokens from blacklist finished.");
    }
}
