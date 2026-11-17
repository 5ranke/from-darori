package com.daroring.fromdarori.global.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret, String issuer, Duration accessTokenTtl,
        Duration refreshTokenTtl, Duration rememberMeRefreshTokenTtl,
        boolean cookieSecure) {

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT_SECRET must be configured as a Base64-encoded key of at least 32 bytes");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("JWT issuer must not be blank");
        }
        requirePositive(accessTokenTtl);
        requirePositive(refreshTokenTtl);
        requirePositive(rememberMeRefreshTokenTtl);
    }

    private static void requirePositive(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Token TTL must be positive");
        }
    }

    @Override
    public String toString() {
        return "JwtProperties[secret=REDACTED, issuer=" + issuer + "]";
    }
}
