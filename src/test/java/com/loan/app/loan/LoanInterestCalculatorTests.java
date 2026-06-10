package com.loan.app.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;

class LoanInterestCalculatorTests {

    @Test
    void totalRepayableUsesMonthlyCompounding() {
        BigDecimal totalRepayable = LoanInterestCalculator.totalRepayable(
            BigDecimal.valueOf(12_000),
            BigDecimal.valueOf(12),
            12,
            0
        );

        assertEquals("13521.90", money(totalRepayable));
    }

    @Test
    void totalInterestIncludesGracePeriodInCompoundTerm() {
        BigDecimal totalInterest = LoanInterestCalculator.totalInterest(
            BigDecimal.valueOf(12_000),
            BigDecimal.valueOf(12),
            12,
            30
        );

        assertEquals("1657.12", money(totalInterest));
    }

    @Test
    void monthlyInstallmentIsBasedOnCompoundedTotalRepayable() {
        BigDecimal monthlyInstallment = LoanInterestCalculator.monthlyInstallment(
            BigDecimal.valueOf(12_000),
            BigDecimal.valueOf(12),
            12,
            0
        );

        assertEquals("1126.83", money(monthlyInstallment));
    }

    @Test
    void accruedInterestUsesCompoundPeriods() {
        BigDecimal accruedInterest = LoanInterestCalculator.accruedInterest(
            BigDecimal.valueOf(12_000),
            BigDecimal.valueOf(12),
            3
        );

        assertEquals("363.61", money(accruedInterest));
    }

    private String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
