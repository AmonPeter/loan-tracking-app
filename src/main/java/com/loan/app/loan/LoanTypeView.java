package com.loan.app.loan;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LoanTypeView(
    Long id,
    String name,
    BigDecimal interestRate,
    Boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
