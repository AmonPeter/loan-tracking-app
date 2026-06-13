package com.loan.app.loan;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record InterestRateSettingView(
    Long id,
    BigDecimal interestRate,
    OffsetDateTime createdAt
) {
}
