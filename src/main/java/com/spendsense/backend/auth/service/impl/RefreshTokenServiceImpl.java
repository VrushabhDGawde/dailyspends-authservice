package com.spendsense.backend.auth.service.impl;

import com.spendsense.backend.auth.dto.request.RefreshTokenRequest;
import com.spendsense.backend.auth.dto.response.RefreshTokenResponse;
import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.entity.RefreshToken;
import com.spendsense.backend.auth.repository.RefreshTokenRepository;
import com.spendsense.backend.auth.service.RefreshTokenService;
import com.spendsense.backend.common.exception.InvalidRefreshTokenException;
import com.spendsense.backend.security.jwt.JwtService;
import com.spendsense.backend.security.service.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

        private final RefreshTokenRepository refreshTokenRepository;
        private final JwtService jwtService;

        @Override
        @Transactional
        public RefreshToken createRefreshToken(AppUser appUser) {
                log.info("Creating new refresh token for user: {}", appUser.getEmail());

                refreshTokenRepository.findByAppUser(appUser)
                                .ifPresent(token -> {
                                        log.debug("Deleting existing refresh token for user: {}", appUser.getEmail());
                                        refreshTokenRepository.delete(token);
                                });

                refreshTokenRepository.flush();

                UserPrincipal userPrincipal = new UserPrincipal(appUser);
                String refreshToken = jwtService.generateRefreshToken(userPrincipal);

                RefreshToken token = RefreshToken.builder()
                                .token(refreshToken)
                                .appUser(appUser)
                                .revoked(false)
                                .build();

                RefreshToken savedToken = refreshTokenRepository.save(token);
                log.info("New refresh token created successfully for user: {}", appUser.getEmail());
                return savedToken;
        }

        @Override
        public Optional<RefreshToken> findByToken(String token) {
                return refreshTokenRepository.findByToken(token);
        }

        @Override
        public RefreshToken verifyToken(RefreshToken refreshToken) {
                log.info("Verifying refresh token for user: {}",
                                refreshToken.getAppUser() != null ? refreshToken.getAppUser().getEmail() : "unknown");

                if (refreshToken.isRevoked()) {
                        log.warn("Token verification failed: Refresh token is revoked");
                        throw new InvalidRefreshTokenException("Refresh token has been revoked.");
                }

                UserPrincipal principal = new UserPrincipal(refreshToken.getAppUser());

                if (!jwtService.isTokenValid(refreshToken.getToken(), principal)) {
                        log.warn("Token verification failed: JWT is invalid or expired");
                        throw new InvalidRefreshTokenException("Invalid refresh token.");
                }

                log.info("Refresh token verified successfully");
                return refreshToken;
        }

        @Override
        public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
                log.info("Attempting to refresh access token via RefreshTokenService");

                RefreshToken refreshToken = findByToken(request.getRefreshToken())
                                .map(this::verifyToken)
                                .orElseThrow(() -> {
                                        log.warn("Refresh token not found");
                                        return new InvalidRefreshTokenException("Refresh token not found");
                                });

                UserPrincipal principal = new UserPrincipal(refreshToken.getAppUser());
                String accessToken = jwtService.generateAccessToken(principal);

                return RefreshTokenResponse.builder()
                                .accessToken(accessToken)
                                .build();
        }

        @Override
        @Transactional
        public void revokeRefreshToken(AppUser appUser) {
                log.info("Revoking refresh token for user: {}", appUser.getEmail());

                refreshTokenRepository.findByAppUser(appUser)
                                .ifPresent(token -> {
                                        refreshTokenRepository.delete(token);
                                        log.debug("Refresh token deleted for user: {}", appUser.getEmail());
                                });

                refreshTokenRepository.flush();
                log.info("Refresh token revoked successfully for user: {}", appUser.getEmail());
        }
}