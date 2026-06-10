package com.loan.app.loan;

import java.math.BigDecimal;

public record ActiveLoanPortfolioSummary(
    long activeLoanCount,
    BigDecimal totalDisbursed,
    BigDecimal totalRepayable,
    BigDecimal totalPaid,
    BigDecimal totalOutstanding
) {
}
