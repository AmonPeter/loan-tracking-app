package com.loan.app.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuarterlyFundPerformanceRow(
    String quarterLabel,
    LocalDate quarterStartDate,
    LocalDate quarterEndDate,
    BigDecimal openingFundValue,
    BigDecimal disbursedAmount,
    BigDecimal interestAccrued,
    BigDecimal repaymentsReceived,
    BigDecimal closingFundValue,
    long activeLoanCount
) {
}
