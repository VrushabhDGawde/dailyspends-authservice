package com.spendsense.backend.auth.service.impl;

import com.spendsense.backend.auth.dto.RegisterRequest;
import com.spendsense.backend.auth.dto.RegisterResponse;
import com.spendsense.backend.auth.dto.request.GoogleLoginRequest;
import com.spendsense.backend.auth.dto.request.AppleLoginRequest;
import com.spendsense.backend.auth.dto.request.LoginRequest;
import com.spendsense.backend.auth.dto.request.RefreshTokenRequest;
import com.spendsense.backend.auth.dto.response.LoginResponse;
import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.entity.RefreshToken;
import com.spendsense.backend.auth.repository.AppUserRepository;
import com.spendsense.backend.auth.service.AuthService;
import com.spendsense.backend.auth.service.RefreshTokenService;
import com.spendsense.backend.common.enums.Role;
import com.spendsense.backend.common.exception.EmailAlreadyExistsException;
import com.spendsense.backend.common.exception.InvalidCredentialsException;
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
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

        private final AppUserRepository appUserRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;
        private final RestTemplate restTemplate = new RestTemplate();

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
        public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
                log.info("Attempting Google login/registration");

                String email = null;
                String fullName = null;

                try {
                        String verifyUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();
                        Map<String, Object> payload = restTemplate.getForObject(verifyUrl, Map.class);

                        if (payload != null && payload.containsKey("email")) {
                                email = (String) payload.get("email");
                                fullName = (String) payload.getOrDefault("name", email.split("@")[0]);
                        }
                } catch (Exception e) {
                        log.warn("Google tokeninfo verification failed: {}", e.getMessage());
                }

                if (email == null || email.isBlank()) {
                        throw new InvalidCredentialsException("Invalid or expired Google ID Token.");
                }

                email = email.trim().toLowerCase();
                AppUser appUser = appUserRepository.findByEmail(email).orElse(null);

                if (appUser == null) {
                        log.info("Creating new Google user account for email: {}", email);
                        appUser = AppUser.builder()
                                        .fullName(fullName != null ? fullName : email.split("@")[0])
                                        .email(email)
                                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                        .role(Role.USER)
                                        .enabled(true)
                                        .emailVerified(true)
                                        .build();
                        appUser = appUserRepository.save(appUser);
                } else {
                        log.info("Existing Google user account loaded for email: {}", email);
                }

                UserPrincipal userPrincipal = new UserPrincipal(appUser);
                String accessToken = jwtService.generateAccessToken(userPrincipal);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(appUser);

                return LoginResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken.getToken())
                                .build();
        }

        @Override
        @Transactional
        public LoginResponse loginWithApple(AppleLoginRequest request) {
                log.info("Processing Apple Sign-In request");

                String email = request.getEmail();
                String fullName = request.getFullName();

                // If email is not directly passed (subsequent logins), use userIdentifier or identityToken payload
                if ((email == null || email.isBlank()) && request.getUserIdentifier() != null) {
                        email = request.getUserIdentifier().toLowerCase() + "@privaterelay.appleid.com";
                }

                if (email == null || email.isBlank()) {
                        throw new InvalidCredentialsException("Invalid or missing Apple User Credentials.");
                }

                email = email.trim().toLowerCase();
                AppUser appUser = appUserRepository.findByEmail(email).orElse(null);

                if (appUser == null) {
                        log.info("Creating new Apple user account for email: {}", email);
                        appUser = AppUser.builder()
                                        .fullName(fullName != null && !fullName.isBlank() ? fullName : "Apple User")
                                        .email(email)
                                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                        .role(Role.USER)
                                        .enabled(true)
                                        .emailVerified(true)
                                        .build();
                        appUser = appUserRepository.save(appUser);
                } else {
                        log.info("Existing Apple user account loaded for email: {}", email);
                }

                UserPrincipal userPrincipal = new UserPrincipal(appUser);
                String accessToken = jwtService.generateAccessToken(userPrincipal);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(appUser);

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