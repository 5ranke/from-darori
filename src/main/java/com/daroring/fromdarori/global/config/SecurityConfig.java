package com.daroring.fromdarori.global.config;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("role");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter authentication = new JwtAuthenticationConverter();
        authentication.setJwtGrantedAuthoritiesConverter(authorities);

        return http
                // API authentication uses the Authorization header, never the refresh cookie.
                // Cookie-based refresh must stay POST-only with SameSite=Strict cookies.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/auth/login",
                                "/api/v1/admin/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("OPERATOR", "REPRESENTATIVE")
                        .anyRequest().permitAll())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> unauthorized(response))
                        .accessDeniedHandler((request, response, exception) -> writeError(response,
                                403, "AUTH4031", "접근 권한이 없습니다.")))
                .oauth2ResourceServer(resource -> resource
                        .bearerTokenResolver(request -> {
                            // Refresh authenticates solely with its cookie, even with an expired Bearer header.
                            if ("POST".equals(request.getMethod()) && "/api/v1/admin/auth/refresh".equals(
                                    request.getRequestURI().substring(request.getContextPath().length()))) {
                                return null;
                            }
                            return new DefaultBearerTokenResolver().resolve(request);
                        })
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(authentication))
                        .authenticationEntryPoint((request, response, exception) -> unauthorized(response))
                        .accessDeniedHandler((request, response, exception) -> writeError(response,
                                403, "AUTH4031", "접근 권한이 없습니다.")))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void unauthorized(HttpServletResponse response) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        writeError(response, 401, "AUTH4010", "인증이 필요하거나 토큰이 유효하지 않습니다.");
    }

    private static void writeError(HttpServletResponse response, int status, String code,
                                   String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"isSuccess\":false,\"code\":\"" + code
                + "\",\"message\":\"" + message + "\",\"result\":null}");
    }
}
