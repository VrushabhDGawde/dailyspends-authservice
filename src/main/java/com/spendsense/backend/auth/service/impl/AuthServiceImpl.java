package com.spendsense.backend.auth.service.impl;

import com.spendsense.backend.auth.dto.RegisterRequest;
import com.spendsense.backend.auth.dto.RegisterResponse;
import com.spendsense.backend.auth.dto.request.LoginRequest;
import com.spendsense.backend.auth.dto.request.RefreshTokenRequest;
import com.spendsense.backend.auth.dto.response.LoginResponse;
import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.entity.RefreshToken;
import com.spendsense.backend.auth.repository.AppUserRepository;
import com.spendsense.backend.auth.service.AuthService;
import com.spendsense.backend.auth.service.RefreshTokenService;
import com.spendsense.backend.common.exception.EmailAlreadyExistsException;
import com.spendsense.backend.common.exception.InvalidRefreshTokenException;
import com.spendsense.backend.security.jwt.JwtService;
import com.spendsense.backend.security.service.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

        private final AppUserRepository appUserRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;

        @Override
        @Transactional
        public RegisterResponse register(RegisterRequest request) {
                log.info("Attempting to register user with email: {}", request.getEmail());

                if (appUserRepository.existsByEmail(request.getEmail())) {
                        log.warn("Registration failed. Email already exists: {}", request.getEmail());
                        throw new EmailAlreadyExistsException("Email already exists.");
                }

                AppUser appUser = AppUser.builder()
                                .fullName(request.getFullName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .build();

                AppUser savedUser = appUserRepository.save(appUser);
                log.info("User registered successfully with UUID: {}", savedUser.getUuid());

                return RegisterResponse.builder()
                                .userId(savedUser.getUuid())
                                .fullName(savedUser.getFullName())
                                .email(savedUser.getEmail())
                                .message("User registered successfully.")
                                .build();
        }

        @Override
        public LoginResponse login(LoginRequest request) {
                log.info("Attempting login for email: {}", request.getEmail());

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                AppUser appUser = userPrincipal.getAppUser();

                String accessToken = jwtService.generateAccessToken(userPrincipal);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(appUser);

                log.info("User logged in successfully: {}", request.getEmail());

                return LoginResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken.getToken())
                                .build();
        }

        @Override
        @Transactional
        public LoginResponse refreshToken(RefreshTokenRequest request) {
                log.info("Attempting to refresh access token");

                RefreshToken refreshToken = refreshTokenService
                                .findByToken(request.getRefreshToken())
                                .map(refreshTokenService::verifyToken)
                                .orElseThrow(() -> {
                                        log.warn("Token refresh failed: Invalid refresh token");
                                        return new InvalidRefreshTokenException("Invalid refresh token");
                                });

                AppUser appUser = refreshToken.getAppUser();
                UserPrincipal userPrincipal = new UserPrincipal(appUser);

                String accessToken = jwtService.generateAccessToken(userPrincipal);

                // Rotate refresh token (RTR)
                RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(appUser);

                log.info("Token refreshed successfully for user: {}", appUser.getEmail());

                return LoginResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(newRefreshToken.getToken())
                                .build();
        }

        @Override
        @Transactional
        public void logout(String refreshToken) {
                log.info("Attempting logout");

                RefreshToken token = refreshTokenService
                                .findByToken(refreshToken)
                                .orElseThrow(() -> {
                                        log.warn("Logout failed: Invalid refresh token");
                                        return new InvalidRefreshTokenException("Invalid refresh token");
                                });

                AppUser appUser = token.getAppUser();
                refreshTokenService.revokeRefreshToken(appUser);
                log.info("User logged out successfully: {}", appUser.getEmail());
        }
}