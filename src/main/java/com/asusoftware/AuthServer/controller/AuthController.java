package com.asusoftware.AuthServer.controller;

import com.asusoftware.AuthServer.dto.*;
import com.asusoftware.AuthServer.service.AuthService;
import com.asusoftware.AuthServer.utils.CookieUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        JwtResponse jwt = authService.login(request, httpRequest);

        if (isBrowser(httpRequest)) {
            CookieUtils.addJwtCookies(response, jwt.accessToken(), jwt.refreshToken());
            return ResponseEntity.ok("Login success"); // or redirect
        } else {
            return ResponseEntity.ok(jwt); // JSON for mobile
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        authService.register(request, httpRequest);
        return ResponseEntity.ok("User registered successfully.");
    }

    @GetMapping("/google-login")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }


    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.refreshToken(request, httpRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        CookieUtils.clearJwtCookies(response);
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token, HttpServletRequest httpRequest) {
        return authService.verifyEmail(token, httpRequest);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        authService.forgotPassword(request.email(), httpRequest);
        return ResponseEntity.ok("Password reset email sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        authService.resetPassword(request.token(), request.newPassword(), httpRequest);
        return ResponseEntity.ok("Password reset successfully");
    }



    private boolean isBrowser(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }
}
