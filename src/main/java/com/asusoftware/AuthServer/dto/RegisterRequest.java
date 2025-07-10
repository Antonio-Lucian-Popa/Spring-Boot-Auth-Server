package com.asusoftware.AuthServer.dto;

public record RegisterRequest(
        String username,
        String email,
        String password,
        String firstName,
        String lastName
) {}
