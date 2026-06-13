package com.loan.app.loan;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class InterestRateService {
    private final JdbcClient jdbcClient;

    public InterestRateService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<LoanTypeView> activeLoanTypeRates() {
        return jdbcClient.sql("""
            SELECT
                id,
                name,
                interest_rate AS interestRate,
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

    public BigDecimal requireRateForLoanType(long loanTypeId) {
        return jdbcClient.sql("""
            SELECT interest_rate
            FROM loan_types
            WHERE id = :loanTypeId
              AND active = TRUE
            """)
            .param("loanTypeId", loanTypeId)
            .query(BigDecimal.class)
            .optional()
            .orElseThrow(() -> new IllegalArgumentException("Please select an active loan type with a configured interest rate."));
    }

    public void saveRate(long loanTypeId, BigDecimal interestRate) {
        validate(interestRate);

        int updated = jdbcClient.sql("""
            UPDATE loan_types
            SET interest_rate = :interestRate,
                updated_at = :updatedAt
            WHERE id = :loanTypeId
              AND active = TRUE
            """)
            .param("loanTypeId", loanTypeId)
            .param("interestRate", interestRate)
            .param("updatedAt", OffsetDateTime.now())
            .update();
        if (updated == 0) {
            throw new IllegalArgumentException("Active loan type not found.");
        }
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
