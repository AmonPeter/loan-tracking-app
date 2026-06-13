package com.loan.app.loan;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class InterestRateService {
    private final JdbcClient jdbcClient;

    public InterestRateService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<BigDecimal> currentRate() {
        return jdbcClient.sql("""
            SELECT interest_rate
            FROM interest_rate_settings
            ORDER BY created_at DESC, id DESC
            LIMIT 1
            """)
            .query(BigDecimal.class)
            .optional();
    }

    public BigDecimal requireCurrentRate() {
        return currentRate()
            .orElseThrow(() -> new IllegalArgumentException("Please configure the current interest rate before creating a loan application."));
    }

    public List<InterestRateSettingView> recentRates() {
        return jdbcClient.sql("""
            SELECT
                id,
                interest_rate AS interestRate,
                created_at AS createdAt
            FROM interest_rate_settings
            ORDER BY created_at DESC, id DESC
            LIMIT 10
            """)
            .query(InterestRateSettingView.class)
            .list();
    }

    public void saveRate(BigDecimal interestRate) {
        validate(interestRate);

        jdbcClient.sql("""
            INSERT INTO interest_rate_settings (interest_rate, created_at)
            VALUES (:interestRate, :createdAt)
            """)
            .param("interestRate", interestRate)
            .param("createdAt", OffsetDateTime.now())
            .update();
    }

    private void validate(BigDecimal interestRate) {
        if (interestRate == null) {
            throw new IllegalArgumentException("Interest rate is required.");
        }
        if (interestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate must be zero or greater.");
        }
    }
}
