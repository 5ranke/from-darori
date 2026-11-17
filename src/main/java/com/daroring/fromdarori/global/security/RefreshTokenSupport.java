package com.daroring.fromdarori.global.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenSupport {

    private static final String COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/api/v1/admin/auth";
    private final SecureRandom random = new SecureRandom();
    private final JwtProperties properties;

    // Store only tokenHash and expiresAt in admin_user; send token only via the cookie.
    public IssuedRefreshToken issue(boolean rememberMe) {
        Duration ttl = rememberMe ? properties.rememberMeRefreshTokenTtl() : properties.refreshTokenTtl();
        return generate(Instant.now().plus(ttl), ttl);
    }

    public IssuedRefreshToken issueUntil(Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("Refresh token expiry must be in the future");
        }
        return generate(expiresAt, ttl);
    }

    private IssuedRefreshToken generate(Instant expiresAt, Duration ttl) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new IssuedRefreshToken(token, hash(token), expiresAt, ttl);
    }

    public String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public boolean matches(String token, String storedHash) {
        return token != null && storedHash != null && MessageDigest.isEqual(
                hash(token).getBytes(StandardCharsets.US_ASCII),
                storedHash.getBytes(StandardCharsets.US_ASCII));
    }

    public ResponseCookie cookie(IssuedRefreshToken issued) {
        return cookieBuilder(issued.token()).maxAge(issued.ttl()).build();
    }

    public ResponseCookie clearCookie() {
        return cookieBuilder("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder cookieBuilder(String token) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true).secure(properties.cookieSecure())
                .sameSite("Strict").path(COOKIE_PATH);
    }

    // Avoid record-generated toString exposing the refresh token in logs.
    public record IssuedRefreshToken(String token, String tokenHash, Instant expiresAt, Duration ttl) {
        @Override
        public String toString() {
            return "IssuedRefreshToken[REDACTED, expiresAt=" + expiresAt + "]";
        }
    }
}
