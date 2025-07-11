package com.asusoftware.AuthServer.service;

import com.asusoftware.AuthServer.dto.JwtResponse;
import com.asusoftware.AuthServer.dto.LoginRequest;
import com.asusoftware.AuthServer.dto.RefreshTokenRequest;
import com.asusoftware.AuthServer.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    void register(RegisterRequest request);
    JwtResponse refreshToken(RefreshTokenRequest request);
    ResponseEntity<String> verifyEmail(String token);

}
