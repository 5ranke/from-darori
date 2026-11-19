package com.daroring.fromdarori.domain.admin.service;

import com.daroring.fromdarori.domain.admin.dto.AdminLoginRequest;
import com.daroring.fromdarori.domain.admin.dto.AdminLoginResponse;
import com.daroring.fromdarori.domain.admin.dto.AdminRefreshResponse;
import com.daroring.fromdarori.domain.admin.dto.AdminMeResponse;
import com.daroring.fromdarori.domain.admin.exception.DeletedAdminException;
import java.util.UUID;
import com.daroring.fromdarori.domain.admin.exception.RefreshTokenException;
import com.daroring.fromdarori.domain.admin.exception.RefreshTokenException.Reason;
import com.daroring.fromdarori.domain.admin.entity.AdminUser;
import com.daroring.fromdarori.domain.admin.repository.AdminUserRepository;
import com.daroring.fromdarori.global.security.JwtTokenProvider;
import com.daroring.fromdarori.global.security.RefreshTokenSupport;
import java.time.ZoneOffset;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenSupport refreshTokenSupport;
    // Unknown accounts also run password verification to reduce timing differences.
    private final String dummyPasswordHash;

    public AdminAuthService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder,
                            JwtTokenProvider jwtTokenProvider, RefreshTokenSupport refreshTokenSupport) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenSupport = refreshTokenSupport;
        this.dummyPasswordHash = passwordEncoder.encode(java.util.UUID.randomUUID().toString());
    }

    @Transactional
    public LoginResult login(AdminLoginRequest request) {
        AdminUser user = adminUserRepository.findByEmail(request.email()).orElse(null);
        String hash = user == null ? dummyPasswordHash : user.getPasswordHash();
        boolean passwordMatches = passwordEncoder.matches(request.password(), hash);
        if (user == null || !passwordMatches) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        var refreshToken = refreshTokenSupport.issue(request.isRememberMe());
        user.updateRefreshToken(refreshToken.tokenHash(), refreshToken.expiresAt().atOffset(ZoneOffset.UTC));
        // The managed entity is flushed at transaction commit, before the controller sets the cookie.
        var response = new AdminLoginResponse(accessToken, "Bearer", jwtTokenProvider.accessTokenExpiresIn(),
                AdminLoginResponse.AdminUserInfo.from(user));
        return new LoginResult(response, refreshTokenSupport.cookie(refreshToken));
    }

    @Transactional
    public RefreshResult refresh(String token) {
        if (token == null || token.isBlank()) {
            throw new RefreshTokenException(Reason.MISSING);
        }
        if (!token.matches("[A-Za-z0-9_-]{43}")) {
            throw new RefreshTokenException(Reason.INVALID);
        }
        String oldHash = refreshTokenSupport.hash(token);
        AdminUser user = adminUserRepository.findByRefreshTokenHash(oldHash)
                .orElseThrow(() -> new RefreshTokenException(Reason.INVALID));
        var expiresAt = user.getRefreshTokenExpiresAt();
        if (expiresAt == null) {
            throw new RefreshTokenException(Reason.INVALID);
        }
        if (!expiresAt.toInstant().isAfter(Instant.now())) {
            throw new RefreshTokenException(Reason.EXPIRED);
        }

        RefreshTokenSupport.IssuedRefreshToken refreshToken;
        try {
            refreshToken = refreshTokenSupport.issueUntil(expiresAt.toInstant());
        } catch (IllegalArgumentException exception) {
            throw new RefreshTokenException(Reason.EXPIRED);
        }
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        int updated = adminUserRepository.rotateRefreshToken(user.getId(), oldHash, refreshToken.tokenHash(), now);
        if (updated != 1) {
            throw new RefreshTokenException(expiresAt.isAfter(now) ? Reason.INVALID : Reason.EXPIRED);
        }
        var response = new AdminRefreshResponse(accessToken, "Bearer", jwtTokenProvider.accessTokenExpiresIn());
        return new RefreshResult(response, refreshTokenSupport.cookie(refreshToken));
    }

    @Transactional
    public ResponseCookie logout(String token) {
        if (token != null && !token.isBlank()) {
            if (!token.matches("[A-Za-z0-9_-]{43}")) {
                throw new RefreshTokenException(Reason.INVALID);
            }
            // Missing/expired/already invalidated sessions are treated as logged out.
            // Match the current hash so an old token cannot revoke a newer login.
            adminUserRepository.invalidateRefreshToken(refreshTokenSupport.hash(token),
                    OffsetDateTime.now(ZoneOffset.UTC));
        }
        return refreshTokenSupport.clearCookie();
    }

    @Transactional(readOnly = true)
    public AdminMeResponse me(UUID adminUserId) {
        AdminUser user = adminUserRepository.findById(adminUserId)
                .orElseThrow(DeletedAdminException::new);
        return AdminMeResponse.from(user);
    }

    public record RefreshResult(AdminRefreshResponse response, ResponseCookie refreshCookie) {
        @Override
        public String toString() { return "RefreshResult[REDACTED]"; }
    }

    public record LoginResult(AdminLoginResponse response, ResponseCookie refreshCookie) {
        @Override
        public String toString() { return "LoginResult[REDACTED]"; }
    }
}
