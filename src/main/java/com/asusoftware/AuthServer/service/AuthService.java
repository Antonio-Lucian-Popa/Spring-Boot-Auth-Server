package com.asusoftware.AuthServer.service;

import com.asusoftware.AuthServer.dto.JwtResponse;
import com.asusoftware.AuthServer.dto.LoginRequest;
import com.asusoftware.AuthServer.dto.RefreshTokenRequest;
import com.asusoftware.AuthServer.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    void register(RegisterRequest request, HttpServletRequest httpRequest);
    JwtResponse refreshToken(RefreshTokenRequest request);
    ResponseEntity<String> verifyEmail(String token);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);

}
