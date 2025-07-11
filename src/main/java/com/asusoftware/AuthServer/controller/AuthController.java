package com.asusoftware.AuthServer.controller;

import com.asusoftware.AuthServer.dto.JwtResponse;
import com.asusoftware.AuthServer.dto.LoginRequest;
import com.asusoftware.AuthServer.dto.RefreshTokenRequest;
import com.asusoftware.AuthServer.dto.RegisterRequest;
import com.asusoftware.AuthServer.service.AuthService;
import com.asusoftware.AuthServer.utils.CookieUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        JwtResponse jwt = authService.login(request);

        if (isBrowser(httpRequest)) {
            CookieUtils.addJwtCookies(response, jwt.accessToken(), jwt.refreshToken());
            return ResponseEntity.ok("Login success"); // or redirect
        } else {
            return ResponseEntity.ok(jwt); // JSON for mobile
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        CookieUtils.clearJwtCookies(response);
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        return authService.verifyEmail(token);
    }



    private boolean isBrowser(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }
}
