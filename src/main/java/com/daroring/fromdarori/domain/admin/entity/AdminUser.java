package com.daroring.fromdarori.domain.admin.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "admin_user")
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "refresh_token_expires_at")
    private OffsetDateTime refreshTokenExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private AdminRole role;

    @Column(name = "refresh_token_hash", length = 255)
    private String refreshTokenHash;

    public static AdminUser create(String displayName, String email, String passwordHash, AdminRole role) {
        AdminUser user = new AdminUser();
        user.displayName = displayName;
        user.email = email;
        user.passwordHash = passwordHash;
        user.role = role;
        return user;
    }

    public void updateRefreshToken(String tokenHash, OffsetDateTime expiresAt) {
        this.refreshTokenHash = tokenHash;
        this.refreshTokenExpiresAt = expiresAt;
    }
}
