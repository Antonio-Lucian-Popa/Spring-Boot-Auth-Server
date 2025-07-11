package com.asusoftware.AuthServer.service.impl;

import com.asusoftware.AuthServer.dto.JwtResponse;
import com.asusoftware.AuthServer.dto.LoginRequest;
import com.asusoftware.AuthServer.dto.RefreshTokenRequest;
import com.asusoftware.AuthServer.dto.RegisterRequest;
import com.asusoftware.AuthServer.entity.Role;
import com.asusoftware.AuthServer.entity.User;
import com.asusoftware.AuthServer.repository.RoleRepository;
import com.asusoftware.AuthServer.repository.UserRepository;
import com.asusoftware.AuthServer.service.AuthService;
import com.asusoftware.AuthServer.service.EmailService;
import com.asusoftware.AuthServer.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Override
    public JwtResponse login(LoginRequest request) {
        var user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new JwtResponse(
                accessToken,
                refreshToken,
                "Bearer",
                user.getEmail(),
                user.getUsername(),
                user.getRoles().stream().findFirst().map(Role::getName).orElse("USER")
        );
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email()) || userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("User already exists");
        }

        var user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(false); // ⚠️ userul NU este activ până confirmă emailul

        var role = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().add(role);
        userRepository.save(user);

        try {
            // ✅ Generăm tokenul și trimitem emailul de verificare
            String token = jwtService.generateEmailVerificationToken(user);
            emailService.sendVerificationEmail(user.getEmail(), token, user.getFirstName());
        } catch (MessagingException e) {
            // Logăm și continuăm
            System.err.println("Failed to send verification email: " + e.getMessage());
        }

    }


    @Override
    public JwtResponse refreshToken(RefreshTokenRequest request) {
        var user = jwtService.extractUserFromRefreshToken(request.refreshToken());
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new JwtResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                user.getEmail(),
                user.getUsername(),
                user.getRoles().stream().findFirst().map(Role::getName).orElse("USER")
        );
    }

    @Override
    public ResponseEntity<String> verifyEmail(String token) {
        try {
            Claims claims = jwtService.extractAllClaims(token);
            String email = claims.getSubject();
            String scope = claims.get("scope", String.class);

            if (!"email_verification".equals(scope)) {
                return ResponseEntity.badRequest().body("Invalid verification token");
            }

            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setEnabled(true); // ⚠️ Activăm utilizatorul după verificarea emailului
            userRepository.save(user);

            return ResponseEntity.ok("Email verified successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
    }

    @Override
    public void forgotPassword(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateResetPasswordToken(user);
        try {
            emailService.sendResetPasswordEmail(user.getEmail(), token, user.getFirstName());
        } catch (MessagingException e) {
            System.err.println("Failed to send reset email: " + e.getMessage());
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        try {
            Claims claims = jwtService.extractAllClaimsFromResetToken(token);
            String email = claims.getSubject();
            String scope = claims.get("scope", String.class);

            if (!"password_reset".equals(scope)) {
                throw new RuntimeException("Invalid token scope");
            }

            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired reset token");
        }
    }



}