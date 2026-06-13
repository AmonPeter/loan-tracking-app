package com.loan.app.loan;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class LoanTypeService {
    private final JdbcClient jdbcClient;

    public LoanTypeService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<LoanTypeView> findActive() {
        return jdbcClient.sql("""
            SELECT
                id,
                name,
                active,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM loan_types
            WHERE active = TRUE
            ORDER BY name
            """)
            .query(LoanTypeView.class)
            .list();
    }

    public List<LoanTypeView> findAll() {
        return jdbcClient.sql("""
            SELECT
                id,
                name,
                active,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM loan_types
            ORDER BY active DESC, name
            """)
            .query(LoanTypeView.class)
            .list();
    }

    public void create(String name) {
        String normalizedName = normalizeName(name);
        ensureUniqueName(normalizedName, null);

        jdbcClient.sql("""
            INSERT INTO loan_types (name, active, created_at, updated_at)
            VALUES (:name, TRUE, :now, :now)
            """)
            .param("name", normalizedName)
            .param("now", OffsetDateTime.now())
            .update();
    }

    public void update(long id, String name, boolean active) {
        String normalizedName = normalizeName(name);
        ensureExists(id);
        ensureUniqueName(normalizedName, id);

        jdbcClient.sql("""
            UPDATE loan_types
            SET name = :name,
                active = :active,
                updated_at = :updatedAt
            WHERE id = :id
            """)
            .param("id", id)
            .param("name", normalizedName)
            .param("active", active)
            .param("updatedAt", OffsetDateTime.now())
            .update();
    }

    private void ensureExists(long id) {
        Boolean exists = jdbcClient.sql("SELECT EXISTS (SELECT 1 FROM loan_types WHERE id = :id)")
            .param("id", id)
            .query(Boolean.class)
            .single();
        if (!Boolean.TRUE.equals(exists)) {
            throw new IllegalArgumentException("Loan type not found.");
        }
    }

    private void ensureUniqueName(String name, Long currentId) {
        String normalized = name.toLowerCase(Locale.ROOT);
        StringBuilder sql = new StringBuilder("""
            SELECT EXISTS (
                SELECT 1
                FROM loan_types
                WHERE lower(name) = :name
            """);
        if (currentId != null) {
            sql.append(" AND id <> :id");
        }
        sql.append(")");

        var statement = jdbcClient.sql(sql.toString())
            .param("name", normalized);
        if (currentId != null) {
            statement = statement.param("id", currentId);
        }

        Boolean exists = statement.query(Boolean.class).single();
        if (Boolean.TRUE.equals(exists)) {
            throw new IllegalArgumentException("Loan type already exists.");
        }
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Loan type name is required.");
        }
        String normalized = name.trim();
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Loan type name must be 100 characters or less.");
        }
        return normalized;
    }
}
