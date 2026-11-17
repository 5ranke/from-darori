package com.daroring.fromdarori.domain.admin.repository;

import com.daroring.fromdarori.domain.admin.entity.AdminUser;
import java.util.Optional;
import java.util.UUID;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {
    Optional<AdminUser> findByEmail(String email);

    Optional<AdminUser> findByRefreshTokenHash(String refreshTokenHash);

    // Compare-and-set: only one concurrent request can consume the old token.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update AdminUser u set u.refreshTokenHash = :newHash, u.updatedAt = :now
            where u.id = :id and u.refreshTokenHash = :oldHash
              and u.refreshTokenExpiresAt > :now
            """)
    int rotateRefreshToken(@Param("id") UUID id, @Param("oldHash") String oldHash,
                           @Param("newHash") String newHash, @Param("now") OffsetDateTime now);
}
