package com.spendsense.backend.auth.service;

import com.spendsense.backend.auth.dto.request.RefreshTokenRequest;
import com.spendsense.backend.auth.dto.response.RefreshTokenResponse;
import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(AppUser appUser);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyToken(RefreshToken refreshToken);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    void revokeRefreshToken(AppUser appUser);
}