package com.loan.app.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ActiveLoanPortfolioRow(
    Long loanNumber,
    String applicantName,
    String loanType,
    String region,
    LocalDate disbursementDate,
    BigDecimal disbursedAmount,
    BigDecimal interestRate,
    Integer durationMonths,
    BigDecimal totalRepayable,
    BigDecimal amountPaid,
    BigDecimal outstandingAmount,
    LocalDate nextRepaymentDate,
    String loanHealth
) {
}
