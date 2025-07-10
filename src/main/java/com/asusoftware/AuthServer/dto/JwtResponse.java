package com.asusoftware.AuthServer.dto;

public record JwtResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String email,
        String username,
        String role
) {}