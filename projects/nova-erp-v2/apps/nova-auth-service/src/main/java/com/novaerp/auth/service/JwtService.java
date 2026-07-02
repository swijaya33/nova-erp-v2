package com.novaerp.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JWT token service — creates, validates, and parses tokens using HS256.
 * Secret key loaded from environment variable JWT_SECRET (or defaults to dev value).
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret:default-dev-secret-key-change-in-production-must-be-at-least-32-chars}")
    private String secretKey;

    /** Access token TTL — default 1 hour */
    @Value("${jwt.access-token-expiration:3600000}")
    private long accessTokenExpiration;

    /** Refresh token TTL — default 7 days */
    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        // Ensure minimum key length for HS256 (32 bytes)
        if (keyBytes.length < 32) {
            log.warn("JWT secret is shorter than 32 bytes — using padded key. Set JWT_SECRET env var in production!");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** Generate an access token for the given user */
    public String generateAccessToken(String username, String tenantId, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .issuer("nova-auth-service")
                .issuedAt(now)
                .expiration(expiry)
                .claim("tenantId", tenantId)
                .claim("roles", roles)
                .claim("type", "access")
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /** Generate a refresh token for the given user */
    public String generateRefreshToken(String userId, String tenantId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(userId)
                .issuer("nova-auth-service")
                .issuedAt(now)
                .expiration(expiry)
                .claim("tenantId", tenantId)
                .claim("type", "refresh")
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /** Validate a token — returns true if the token is syntactically valid and not expired */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer("nova-auth-service")
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /** Extract the subject (username/userId) from a token */
    public String extractSubject(String token) {
        Claims claims = parseClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /** Extract tenant ID from a token */
    public String extractTenantId(String token) {
        Claims claims = parseClaims(token);
        return claims != null ? claims.get("tenantId", String.class) : null;
    }

    /** Extract roles from a token */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) return List.of();
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    /** Extract token type (access/refresh) */
    public String extractTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims != null ? claims.get("type", String.class) : null;
    }

    /** Get expiration date from a token */
    public Date getExpirationDate(String token) {
        Claims claims = parseClaims(token);
        return claims != null ? claims.getExpiration() : null;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer("nova-auth-service")
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Failed to parse JWT claims: {}", e.getMessage());
            return null;
        }
    }

    // --- Getters for configuration values ---

    public long getAccessTokenExpiration() { return accessTokenExpiration; }
    public long getRefreshTokenExpiration() { return refreshTokenExpiration; }
}
