package com.spendsense.backend.auth.service.impl;

import com.spendsense.backend.auth.dto.request.RefreshTokenRequest;
import com.spendsense.backend.auth.dto.response.RefreshTokenResponse;
import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.entity.RefreshToken;
import com.spendsense.backend.auth.repository.RefreshTokenRepository;
import com.spendsense.backend.common.exception.InvalidRefreshTokenException;
import com.spendsense.backend.security.jwt.JwtService;
import com.spendsense.backend.security.service.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

        @Mock
        private RefreshTokenRepository refreshTokenRepository;

        @Mock
        private JwtService jwtService;

        @InjectMocks
        private RefreshTokenServiceImpl refreshTokenService;

        @Test
        void createRefreshToken_Success() {
                // Arrange
                AppUser appUser = AppUser.builder()
                                .email("john@example.com")
                                .build();

                RefreshToken existingToken = RefreshToken.builder()
                                .token("oldToken")
                                .build();

                RefreshToken savedToken = RefreshToken.builder()
                                .token("newToken")
                                .appUser(appUser)
                                .revoked(false)
                                .build();

                when(refreshTokenRepository.findByAppUser(appUser)).thenReturn(Optional.of(existingToken));
                when(jwtService.generateRefreshToken(any(UserPrincipal.class))).thenReturn("newToken");
                when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

                // Act
                RefreshToken result = refreshTokenService.createRefreshToken(appUser);

                // Assert
                assertNotNull(result);
                assertEquals("newToken", result.getToken());
                assertEquals(appUser, result.getAppUser());
                assertFalse(result.isRevoked());

                verify(refreshTokenRepository).findByAppUser(appUser);
                verify(refreshTokenRepository).delete(existingToken);
                verify(refreshTokenRepository).flush();
                verify(jwtService).generateRefreshToken(any(UserPrincipal.class));
                verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        void findByToken_Success() {
                // Arrange
                String tokenStr = "mockToken";
                RefreshToken token = RefreshToken.builder().token(tokenStr).build();
                when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));

                // Act
                Optional<RefreshToken> result = refreshTokenService.findByToken(tokenStr);

                // Assert
                assertTrue(result.isPresent());
                assertEquals(tokenStr, result.get().getToken());
                verify(refreshTokenRepository).findByToken(tokenStr);
        }

        @Test
        void verifyToken_Success() {
                // Arrange
                AppUser appUser = AppUser.builder()
                                .email("john@example.com")
                                .build();
                RefreshToken token = RefreshToken.builder()
                                .token("validToken")
                                .appUser(appUser)
                                .revoked(false)
                                .build();

                when(jwtService.isTokenValid(eq("validToken"), any(UserPrincipal.class))).thenReturn(true);

                // Act
                RefreshToken result = refreshTokenService.verifyToken(token);

                // Assert
                assertNotNull(result);
                assertEquals(token, result);
                verify(jwtService).isTokenValid(eq("validToken"), any(UserPrincipal.class));
        }

        @Test
        void verifyToken_Revoked_ThrowsException() {
                // Arrange
                AppUser appUser = AppUser.builder()
                                .email("john@example.com")
                                .build();
                RefreshToken token = RefreshToken.builder()
                                .token("revokedToken")
                                .appUser(appUser)
                                .revoked(true)
                                .build();

                // Act & Assert
                InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class,
                                () -> refreshTokenService.verifyToken(token));
                assertEquals("Refresh token has been revoked.", exception.getMessage());
                verify(jwtService, never()).isTokenValid(any(), any());
        }

        @Test
        void verifyToken_InvalidJwt_ThrowsException() {
                // Arrange
                AppUser appUser = AppUser.builder()
                                .email("john@example.com")
                                .build();
                RefreshToken token = RefreshToken.builder()
                                .token("invalidToken")
                                .appUser(appUser)
                                .revoked(false)
                                .build();

                when(jwtService.isTokenValid(eq("invalidToken"), any(UserPrincipal.class))).thenReturn(false);

                // Act & Assert
                InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class,
                                () -> refreshTokenService.verifyToken(token));
                assertEquals("Invalid refresh token.", exception.getMessage());
                verify(jwtService).isTokenValid(eq("invalidToken"), any(UserPrincipal.class));
        }

        @Test
        void refreshToken_Success() {
                // Arrange
                RefreshTokenRequest request = RefreshTokenRequest.builder()
                                .refreshToken("validToken")
                                .build();

                AppUser appUser = AppUser.builder()
                                .email("john@example.com")
                                .build();

                RefreshToken token = RefreshToken.builder()
                                .token("validToken")
                                .appUser(appUser)
                                .revoked(false)
                                .build();

                when(refreshTokenRepository.findByToken("validToken")).thenReturn(Optional.of(token));
                when(jwtService.isTokenValid(eq("validToken"), any(UserPrincipal.class))).thenReturn(true);
                when(jwtService.generateAccessToken(any(UserPrincipal.class))).thenReturn("newAccessToken");

                // Act
                RefreshTokenResponse response = refreshTokenService.refreshToken(request);

                // Assert
                assertNotNull(response);
                assertEquals("newAccessToken", response.getAccessToken());

                verify(refreshTokenRepository).findByToken("validToken");
                verify(jwtService).isTokenValid(eq("validToken"), any(UserPrincipal.class));
                verify(jwtService).generateAccessToken(any(UserPrincipal.class));
        }

        @Test
        void refreshToken_NotFound_ThrowsException() {
                // Arrange
                RefreshTokenRequest request = RefreshTokenRequest.builder()
                                .refreshToken("nonExistentToken")
                                .build();

                when(refreshTokenRepository.findByToken("nonExistentToken")).thenReturn(Optional.empty());

                // Act & Assert
                InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class,
                                () -> refreshTokenService.refreshToken(request));
                assertEquals("Refresh token not found", exception.getMessage());

                verify(refreshTokenRepository).findByToken("nonExistentToken");
                verify(jwtService, never()).generateAccessToken(any());
        }

        @Test
        void revokeRefreshToken_Success() {
                // Arrange
                AppUser appUser = AppUser.builder().build();
                RefreshToken token = RefreshToken.builder().build();

                when(refreshTokenRepository.findByAppUser(appUser)).thenReturn(Optional.of(token));

                // Act
                refreshTokenService.revokeRefreshToken(appUser);

                // Assert
                verify(refreshTokenRepository).findByAppUser(appUser);
                verify(refreshTokenRepository).delete(token);
                verify(refreshTokenRepository).flush();
        }
}
