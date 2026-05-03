package com.loan.app.loan;

import java.math.BigDecimal;

public record LoanView(
    Long id,
    String projectDescription,
    String applicantFirstName,
    String applicantSurname,
    String applicantIdNumber,
    String contactNumber,
    String region,
    String townVillage,
    String membershipStatus,
    String gender,
    String conditionsPrecedent,
    BigDecimal interestRate,
    String loanType,
    Integer durationMonths,
    Integer gracePeriodDays,
    String loanStatus,
    String loanStatusComment,
    String loanConditions,
    BigDecimal approvedAmount,
    BigDecimal disbursedAmount
) {
}
