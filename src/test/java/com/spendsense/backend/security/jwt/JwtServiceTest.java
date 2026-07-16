package com.spendsense.backend.security.jwt;

import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.security.service.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;
    private UserPrincipal userPrincipal;
    private AppUser appUser;

    // "12345678901234567890123456789012" in Base64 (32 bytes / 256 bits)
    private static final String BASE64_SECRET = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", BASE64_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 86400000L); // 24 hours

        appUser = AppUser.builder()
                .email("john@example.com")
                .build();
        userPrincipal = new UserPrincipal(appUser);
    }

    @Test
    void generateAccessToken_Success() {
        String token = jwtService.generateAccessToken(userPrincipal);
        assertNotNull(token);
        assertEquals("john@example.com", jwtService.extractUsername(token));
    }

    @Test
    void generateRefreshToken_Success() {
        String token = jwtService.generateRefreshToken(userPrincipal);
        assertNotNull(token);
        assertEquals("john@example.com", jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_Success() {
        String token = jwtService.generateAccessToken(userPrincipal);
        String username = jwtService.extractUsername(token);
        assertEquals("john@example.com", username);
    }

    @Test
    void extractExpiration_Success() {
        String token = jwtService.generateAccessToken(userPrincipal);
        Date expiration = jwtService.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void isTokenValid_Success() {
        String token = jwtService.generateAccessToken(userPrincipal);
        boolean isValid = jwtService.isTokenValid(token, userPrincipal);
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_InvalidUsername_ReturnsFalse() {
        String token = jwtService.generateAccessToken(userPrincipal);
        AppUser otherUser = AppUser.builder().email("other@example.com").build();
        UserPrincipal otherPrincipal = new UserPrincipal(otherUser);

        boolean isValid = jwtService.isTokenValid(token, otherPrincipal);
        assertFalse(isValid);
    }
}
