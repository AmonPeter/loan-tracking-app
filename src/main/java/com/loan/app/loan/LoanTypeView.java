package com.loan.app.loan;

import java.time.OffsetDateTime;

public record LoanTypeView(
    Long id,
    String name,
    Boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
