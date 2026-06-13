package com.loan.app.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanForm(
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
    BigDecimal disbursedAmount,
    LocalDate disbursementDate,
    Integer repaymentStartMonth,
    Integer repaymentStartYear,
    Integer repaymentStartDate
) {
    public LoanForm withInterestRate(BigDecimal interestRate) {
        return new LoanForm(
            projectDescription,
            applicantFirstName,
            applicantSurname,
            applicantIdNumber,
            contactNumber,
            region,
            townVillage,
            membershipStatus,
            gender,
            conditionsPrecedent,
            interestRate,
            loanType,
            durationMonths,
            gracePeriodDays,
            loanStatus,
            loanStatusComment,
            loanConditions,
            approvedAmount,
            disbursedAmount,
            disbursementDate,
            repaymentStartMonth,
            repaymentStartYear,
            repaymentStartDate
        );
    }
}
