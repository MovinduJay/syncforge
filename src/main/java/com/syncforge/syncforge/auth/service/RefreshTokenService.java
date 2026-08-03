package com.syncforge.syncforge.auth.service;

import com.syncforge.syncforge.auth.model.AppUser;
import com.syncforge.syncforge.auth.model.RefreshToken;
import com.syncforge.syncforge.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long refreshTokenDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${syncforge.security.refresh-token-days}") long refreshTokenDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDays = refreshTokenDays;
    }

    public String createRefreshToken(AppUser user) {
        String rawToken = generateRandomToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHash,
                LocalDateTime.now().plusDays(refreshTokenDays)
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    public RefreshToken validateRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (!refreshToken.isUsable()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is expired or revoked");
        }

        return refreshToken;
    }

    public void revokeRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        refreshTokenRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(refreshToken -> {
                    refreshToken.revoke();
                    refreshTokenRepository.save(refreshToken);
                });
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to hash refresh token", exception);
        }
    }
}
