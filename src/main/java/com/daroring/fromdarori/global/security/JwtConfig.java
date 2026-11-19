package com.daroring.fromdarori.global.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] bytes = Base64.getDecoder().decode(properties.secret());
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must decode to at least 32 bytes");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey, JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()),
                new JwtClaimValidator<String>("token_type", "access"::equals),
                new JwtClaimValidator<String>("sub", JwtConfig::isUuid),
                new JwtClaimValidator<String>("role", role ->
                        "OPERATOR".equals(role) || "REPRESENTATIVE".equals(role)),
                new JwtClaimValidator<Object>("exp", value -> value != null)));
        return decoder;
    }

    private static boolean isUuid(String subject) {
        try {
            return subject != null && java.util.UUID.fromString(subject).toString().equals(subject);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
