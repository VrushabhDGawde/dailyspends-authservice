package com.spendsense.backend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.backend.auth.dto.RegisterRequest;
import com.spendsense.backend.auth.dto.RegisterResponse;
import com.spendsense.backend.auth.dto.request.LoginRequest;
import com.spendsense.backend.auth.dto.request.RefreshTokenRequest;
import com.spendsense.backend.auth.dto.response.LoginResponse;
import com.spendsense.backend.auth.service.AuthService;
import com.spendsense.backend.security.jwt.JwtService;
import com.spendsense.backend.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass security filters for unit testing controller logic
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AuthService authService;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;

        @Test
        void register_Success() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .fullName("John Doe")
                                .email("john@example.com")
                                .password("password123")
                                .build();

                RegisterResponse response = RegisterResponse.builder()
                                .userId(UUID.randomUUID())
                                .fullName("John Doe")
                                .email("john@example.com")
                                .message("User registered successfully.")
                                .build();

                when(authService.register(any(RegisterRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.fullName").value("John Doe"))
                                .andExpect(jsonPath("$.email").value("john@example.com"))
                                .andExpect(jsonPath("$.message").value("User registered successfully."));

                verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        void register_InvalidRequest_ReturnsBadRequest() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .fullName("") // Invalid: blank
                                .email("invalid-email") // Invalid: format
                                .password("short") // Invalid: < 8 chars
                                .build();

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());

                verify(authService, never()).register(any());
        }

        @Test
        void login_Success() throws Exception {
                LoginRequest request = LoginRequest.builder()
                                .email("john@example.com")
                                .password("password123")
                                .build();

                LoginResponse response = LoginResponse.builder()
                                .accessToken("mockAccessToken")
                                .refreshToken("mockRefreshToken")
                                .build();

                when(authService.login(any(LoginRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("mockAccessToken"))
                                .andExpect(jsonPath("$.refreshToken").value("mockRefreshToken"));

                verify(authService).login(any(LoginRequest.class));
        }

        @Test
        void login_InvalidRequest_ReturnsBadRequest() throws Exception {
                LoginRequest request = LoginRequest.builder()
                                .email("") // Invalid: blank
                                .password("") // Invalid: blank
                                .build();

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());

                verify(authService, never()).login(any());
        }

        @Test
        void refreshToken_Success() throws Exception {
                RefreshTokenRequest request = RefreshTokenRequest.builder()
                                .refreshToken("mockRefreshToken")
                                .build();

                LoginResponse response = LoginResponse.builder()
                                .accessToken("newAccessToken")
                                .refreshToken("mockRefreshToken")
                                .build();

                when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/v1/auth/refresh-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                                .andExpect(jsonPath("$.refreshToken").value("mockRefreshToken"));

                verify(authService).refreshToken(any(RefreshTokenRequest.class));
        }

        @Test
        void logout_Success() throws Exception {
                RefreshTokenRequest request = RefreshTokenRequest.builder()
                                .refreshToken("mockRefreshToken")
                                .build();

                doNothing().when(authService).logout("mockRefreshToken");

                mockMvc.perform(post("/api/v1/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                verify(authService).logout("mockRefreshToken");
        }
}
