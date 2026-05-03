package com.loan.app.user;

import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public record AppUser(
    @Id Long id,
    String email,
    @Column("password_hash") String passwordHash,
    String role,
    boolean enabled,
    @Column("account_non_expired") boolean accountNonExpired,
    @Column("account_non_locked") boolean accountNonLocked,
    @Column("credentials_non_expired") boolean credentialsNonExpired,
    @Column("created_at") OffsetDateTime createdAt,
    @Column("updated_at") OffsetDateTime updatedAt,
    @Column("last_login_at") OffsetDateTime lastLoginAt
) {
}
