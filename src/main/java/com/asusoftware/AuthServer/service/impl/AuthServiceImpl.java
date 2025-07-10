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
import com.asusoftware.AuthServer.service.JwtService;
import lombok.RequiredArgsConstructor;
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
        user.setId(UUID.randomUUID());
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);

        var role = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().add(role);
        userRepository.save(user);
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
}