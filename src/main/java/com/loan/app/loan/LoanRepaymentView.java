package com.loan.app.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LoanRepaymentView(
    Long id,
    Long loanId,
    BigDecimal paymentAmount,
    LocalDate paymentDate,
    Integer repaymentMonth,
    Integer repaymentYear,
    String paymentNote,
    OffsetDateTime createdAt
) {
}
