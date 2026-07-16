package com.spendsense.backend.auth.service;

import com.spendsense.backend.auth.dto.RegisterRequest;
import com.spendsense.backend.auth.dto.RegisterResponse;
import com.spendsense.backend.auth.dto.request.LoginRequest;
import com.spendsense.backend.auth.dto.response.LoginResponse;
import com.spendsense.backend.auth.dto.request.RefreshTokenRequest;
public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);
}