package com.loan.app.loan;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class LoanInterestCalculator {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);
    private static final double DAYS_PER_MONTH = 30.0;

    private LoanInterestCalculator() {
    }

    static BigDecimal totalRepayable(
        BigDecimal principal,
        BigDecimal annualRatePercent,
        int durationMonths,
        int gracePeriodDays
    ) {
        if (principal == null || annualRatePercent == null || durationMonths <= 0) {
            return BigDecimal.ZERO;
        }
        if (principal.compareTo(BigDecimal.ZERO) <= 0 || annualRatePercent.compareTo(BigDecimal.ZERO) <= 0) {
            return principal.max(BigDecimal.ZERO);
        }

        double monthlyRate = monthlyRate(annualRatePercent).doubleValue();
        double periods = durationMonths + graceMonths(gracePeriodDays);
        double compoundFactor = Math.pow(1.0 + monthlyRate, periods);
        return principal.multiply(BigDecimal.valueOf(compoundFactor));
    }

    static BigDecimal totalInterest(
        BigDecimal principal,
        BigDecimal annualRatePercent,
        int durationMonths,
        int gracePeriodDays
    ) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalRepayable = totalRepayable(principal, annualRatePercent, durationMonths, gracePeriodDays);
        BigDecimal totalInterest = totalRepayable.subtract(principal);
        return totalInterest.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : totalInterest;
    }

    static BigDecimal monthlyInstallment(
        BigDecimal principal,
        BigDecimal annualRatePercent,
        int durationMonths,
        int gracePeriodDays
    ) {
        if (durationMonths <= 0) {
            return BigDecimal.ZERO;
        }
        return totalRepayable(principal, annualRatePercent, durationMonths, gracePeriodDays)
            .divide(BigDecimal.valueOf(durationMonths), 10, RoundingMode.HALF_UP);
    }

    static BigDecimal monthlyCompoundInterest(
        BigDecimal principal,
        BigDecimal annualRatePercent,
        int gracePeriodDays,
        int repaymentPeriodNumber
    ) {
        if (principal == null || annualRatePercent == null || repaymentPeriodNumber <= 0) {
            return BigDecimal.ZERO;
        }
        if (principal.compareTo(BigDecimal.ZERO) <= 0 || annualRatePercent.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyRate = monthlyRate(annualRatePercent);
        double periodsBeforeCurrentMonth = graceMonths(gracePeriodDays) + repaymentPeriodNumber - 1;
        double openingBalance = principal.doubleValue() * Math.pow(1.0 + monthlyRate.doubleValue(), periodsBeforeCurrentMonth);
        return BigDecimal.valueOf(openingBalance).multiply(monthlyRate);
    }

    private static BigDecimal monthlyRate(BigDecimal annualRatePercent) {
        return annualRatePercent
            .divide(ONE_HUNDRED, 10, RoundingMode.HALF_UP)
            .divide(MONTHS_IN_YEAR, 10, RoundingMode.HALF_UP);
    }

    private static double graceMonths(int gracePeriodDays) {
        return Math.max(gracePeriodDays, 0) / DAYS_PER_MONTH;
    }
}
