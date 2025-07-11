package com.asusoftware.AuthServer.service;

import com.asusoftware.AuthServer.entity.Role;
import com.asusoftware.AuthServer.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final Key key;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;

    public JwtService(
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.access-token-expiration-minutes}") long accessTokenMinutes,
            @Value("${auth.jwt.refresh-token-expiration-days}") long refreshTokenDays
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMillis = accessTokenMinutes * 60 * 1000;
        this.refreshTokenExpirationMillis = refreshTokenDays * 24 * 60 * 60 * 1000;
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims, accessTokenExpirationMillis);
    }

    public String generateRefreshToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims, refreshTokenExpirationMillis);
    }

    private String generateToken(String subject, Map<String, Object> claims, long expirationMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(User user) {
        return generateAccessToken(user.getEmail(), Map.of(
                "username", user.getUsername(),
                "roles", user.getRoles().stream().map(Role::getName).toList()
        ));
    }

    public String generateRefreshToken(User user) {
        return generateRefreshToken(user.getEmail(), Map.of(
                "username", user.getUsername()
        ));
    }

    public User extractUserFromRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        String email = claims.getSubject();
        String username = claims.get("username", String.class);

        var user = new User();
        user.setEmail(email);
        user.setUsername(username);
        return user;
    }


    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, String expectedSubject) {
        final String subject = extractEmail(token);
        return (subject.equals(expectedSubject) && !isTokenExpired(token));
    }

    public String generateEmailVerificationToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("scope", "email_verification")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 minute
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateResetPasswordToken(User user) {
        return generateToken(user.getEmail(), Map.of(
                "scope", "password_reset"
        ), 15 * 60 * 1000); // 15 minute valabilitate
    }

    public Claims extractAllClaimsFromResetToken(String token) {
        return extractAllClaims(token); // deja există metoda
    }



    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
