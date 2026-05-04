package com.loan.app.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanReportRow(
    Long loanNumber,
    LocalDate loanStartDate,
    LocalDate loanEndDate,
    BigDecimal disbursedAmount,
    BigDecimal openingBalance,
    BigDecimal interestToBeOwned,
    BigDecimal amountPaidSoFar,
    BigDecimal closingBalance
) {
}
