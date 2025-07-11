package com.asusoftware.AuthServer.service;

import com.asusoftware.AuthServer.dto.JwtResponse;
import com.asusoftware.AuthServer.dto.LoginRequest;
import com.asusoftware.AuthServer.dto.RefreshTokenRequest;
import com.asusoftware.AuthServer.dto.RegisterRequest;
import com.asusoftware.AuthServer.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface AuthService {
    JwtResponse login(LoginRequest request, HttpServletRequest httpRequest);
    void register(RegisterRequest request, HttpServletRequest httpRequest);
    JwtResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest);
    ResponseEntity<String> verifyEmail(String token, HttpServletRequest httpRequest);
    void forgotPassword(String email, HttpServletRequest httpRequest);
    void resetPassword(String token, String newPassword, HttpServletRequest httpRequest);

    User findByUsername(String username);
}

