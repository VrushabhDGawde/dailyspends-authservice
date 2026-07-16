package com.spendsense.backend.auth.service.impl;

import com.spendsense.backend.auth.dto.RegisterRequest;
import com.spendsense.backend.auth.dto.RegisterResponse;
import com.spendsense.backend.auth.dto.request.LoginRequest;
import com.spendsense.backend.auth.dto.request.RefreshTokenRequest;
import com.spendsense.backend.auth.dto.response.LoginResponse;
import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.entity.RefreshToken;
import com.spendsense.backend.auth.repository.AppUserRepository;
import com.spendsense.backend.auth.service.RefreshTokenService;
import com.spendsense.backend.common.exception.EmailAlreadyExistsException;
import com.spendsense.backend.common.exception.InvalidRefreshTokenException;
import com.spendsense.backend.security.jwt.JwtService;
import com.spendsense.backend.security.service.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_Success() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("password123")
                .build();

        UUID userUuid = UUID.randomUUID();
        AppUser savedUser = AppUser.builder()
                .uuid(userUuid)
                .fullName("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .build();

        when(appUserRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(userUuid, response.getUserId());
        assertEquals("John Doe", response.getFullName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("User registered successfully.", response.getMessage());

        verify(appUserRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("password123")
                .build();

        when(appUserRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

        verify(appUserRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder, never()).encode(any());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        AppUser appUser = AppUser.builder()
                .email("john@example.com")
                .build();
        UserPrincipal userPrincipal = new UserPrincipal(appUser);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("mockRefreshToken")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateAccessToken(userPrincipal)).thenReturn("mockAccessToken");
        when(refreshTokenService.createRefreshToken(appUser)).thenReturn(refreshToken);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mockAccessToken", response.getAccessToken());
        assertEquals("mockRefreshToken", response.getRefreshToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccessToken(userPrincipal);
        verify(refreshTokenService).createRefreshToken(appUser);
    }

    @Test
    void refreshToken_Success() {
        // Arrange
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("mockRefreshToken")
                .build();

        AppUser appUser = AppUser.builder()
                .email("john@example.com")
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .token("mockRefreshToken")
                .appUser(appUser)
                .build();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token("newRefreshToken")
                .appUser(appUser)
                .build();

        when(refreshTokenService.findByToken("mockRefreshToken")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyToken(refreshToken)).thenReturn(refreshToken);
        when(jwtService.generateAccessToken(any(UserPrincipal.class))).thenReturn("newAccessToken");
        when(refreshTokenService.createRefreshToken(appUser)).thenReturn(newRefreshToken);

        // Act
        LoginResponse response = authService.refreshToken(request);

        // Assert
        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", response.getRefreshToken());

        verify(refreshTokenService).findByToken("mockRefreshToken");
        verify(refreshTokenService).verifyToken(refreshToken);
        verify(jwtService).generateAccessToken(any(UserPrincipal.class));
        verify(refreshTokenService).createRefreshToken(appUser);
    }

    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        // Arrange
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalidToken")
                .build();

        when(refreshTokenService.findByToken("invalidToken")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRefreshTokenException.class, () -> authService.refreshToken(request));

        verify(refreshTokenService).findByToken("invalidToken");
        verify(refreshTokenService, never()).verifyToken(any());
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void logout_Success() {
        // Arrange
        String tokenStr = "mockRefreshToken";
        AppUser appUser = AppUser.builder().build();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenStr)
                .appUser(appUser)
                .build();

        when(refreshTokenService.findByToken(tokenStr)).thenReturn(Optional.of(refreshToken));

        // Act
        authService.logout(tokenStr);

        // Assert
        verify(refreshTokenService).findByToken(tokenStr);
        verify(refreshTokenService).revokeRefreshToken(appUser);
    }

    @Test
    void logout_InvalidToken_ThrowsException() {
        // Arrange
        String tokenStr = "invalidToken";
        when(refreshTokenService.findByToken(tokenStr)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRefreshTokenException.class, () -> authService.logout(tokenStr));

        verify(refreshTokenService).findByToken(tokenStr);
        verify(refreshTokenService, never()).revokeRefreshToken(any());
    }
}
