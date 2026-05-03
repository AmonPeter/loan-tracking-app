package com.loan.app.user;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAdminService {

    private final JdbcClient jdbcClient;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(JdbcClient jdbcClient, PasswordEncoder passwordEncoder) {
        this.jdbcClient = jdbcClient;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminUserView> findAllUsers() {
        return jdbcClient.sql("""
            SELECT id, email, role, enabled
            FROM users
            ORDER BY created_at DESC
            """)
            .query(AdminUserView.class)
            .list();
    }

    public void createUser(String email, String rawPassword, String role, boolean enabled) {
        validateEmail(email);
        validateRole(role);
        validatePassword(rawPassword);

        jdbcClient.sql("""
            INSERT INTO users (
                email, password_hash, role, enabled, account_non_expired,
                account_non_locked, credentials_non_expired, created_at, updated_at
            ) VALUES (
                :email, :passwordHash, :role, :enabled, true, true, true, :createdAt, :updatedAt
            )
            """)
            .param("email", email.trim().toLowerCase(Locale.ROOT))
            .param("passwordHash", passwordEncoder.encode(rawPassword))
            .param("role", normalizeRole(role))
            .param("enabled", enabled)
            .param("createdAt", OffsetDateTime.now())
            .param("updatedAt", OffsetDateTime.now())
            .update();
    }

    public void updateUser(long id, String role, boolean enabled, String rawPassword) {
        validateRole(role);
        String normalizedRole = normalizeRole(role);

        if (rawPassword != null && !rawPassword.isBlank()) {
            validatePassword(rawPassword);
            jdbcClient.sql("""
                UPDATE users
                SET role = :role, enabled = :enabled, password_hash = :passwordHash, updated_at = :updatedAt
                WHERE id = :id
                """)
                .param("id", id)
                .param("role", normalizedRole)
                .param("enabled", enabled)
                .param("passwordHash", passwordEncoder.encode(rawPassword))
                .param("updatedAt", OffsetDateTime.now())
                .update();
            return;
        }

        jdbcClient.sql("""
            UPDATE users
            SET role = :role, enabled = :enabled, updated_at = :updatedAt
            WHERE id = :id
            """)
            .param("id", id)
            .param("role", normalizedRole)
            .param("enabled", enabled)
            .param("updatedAt", OffsetDateTime.now())
            .update();
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
    }

    private void validateRole(String role) {
        String normalizedRole = normalizeRole(role);
        if (!"ADMIN".equals(normalizedRole) && !"USER".equals(normalizedRole)) {
            throw new DataIntegrityViolationException("Role must be USER or ADMIN.");
        }
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    }
}
