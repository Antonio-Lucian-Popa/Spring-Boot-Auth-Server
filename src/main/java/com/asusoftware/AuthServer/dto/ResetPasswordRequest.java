package com.asusoftware.AuthServer.dto;

public record ResetPasswordRequest(
        String token,
        String newPassword
) {}