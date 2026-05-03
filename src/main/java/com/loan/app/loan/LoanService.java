package com.loan.app.loan;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class LoanService {

    private final JdbcClient jdbcClient;

    public LoanService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<LoanView> findAll() {
        return jdbcClient.sql("""
            SELECT
                id,
                project_description AS projectDescription,
                applicant_first_name AS applicantFirstName,
                applicant_surname AS applicantSurname,
                applicant_id_number AS applicantIdNumber,
                contact_number AS contactNumber,
                region,
                town_village AS townVillage,
                membership_status AS membershipStatus,
                gender,
                conditions_precedent AS conditionsPrecedent,
                interest_rate AS interestRate,
                loan_type AS loanType,
                loan_status AS loanStatus,
                loan_status_comment AS loanStatusComment,
                loan_conditions AS loanConditions,
                approved_amount AS approvedAmount,
                disbursed_amount AS disbursedAmount
            FROM loans
            ORDER BY created_at DESC
            """)
            .query(LoanView.class)
            .list();
    }

    public void create(LoanForm form) {
        validate(form);
        OffsetDateTime now = OffsetDateTime.now();

        jdbcClient.sql("""
            INSERT INTO loans (
                project_description, applicant_first_name, applicant_surname,
                applicant_id_number, contact_number, region, town_village,
                membership_status, gender, conditions_precedent, interest_rate,
                loan_type, loan_status, loan_status_comment, loan_conditions,
                approved_amount, disbursed_amount, created_at, updated_at
            ) VALUES (
                :projectDescription, :applicantFirstName, :applicantSurname,
                :applicantIdNumber, :contactNumber, :region, :townVillage,
                :membershipStatus, :gender, :conditionsPrecedent, :interestRate,
                :loanType, :loanStatus, :loanStatusComment, :loanConditions,
                :approvedAmount, :disbursedAmount, :createdAt, :updatedAt
            )
            """)
            .param("projectDescription", form.projectDescription().trim())
            .param("applicantFirstName", form.applicantFirstName().trim())
            .param("applicantSurname", form.applicantSurname().trim())
            .param("applicantIdNumber", form.applicantIdNumber().trim())
            .param("contactNumber", form.contactNumber().trim())
            .param("region", form.region().trim())
            .param("townVillage", form.townVillage().trim())
            .param("membershipStatus", form.membershipStatus().trim())
            .param("gender", form.gender().trim())
            .param("conditionsPrecedent", normalizeOptional(form.conditionsPrecedent()))
            .param("interestRate", form.interestRate())
            .param("loanType", form.loanType().trim())
            .param("loanStatus", form.loanStatus().trim())
            .param("loanStatusComment", normalizeOptional(form.loanStatusComment()))
            .param("loanConditions", normalizeOptional(form.loanConditions()))
            .param("approvedAmount", form.approvedAmount())
            .param("disbursedAmount", form.disbursedAmount())
            .param("createdAt", now)
            .param("updatedAt", now)
            .update();
    }

    public void update(long id, LoanForm form) {
        validate(form);

        jdbcClient.sql("""
            UPDATE loans
            SET
                project_description = :projectDescription,
                applicant_first_name = :applicantFirstName,
                applicant_surname = :applicantSurname,
                applicant_id_number = :applicantIdNumber,
                contact_number = :contactNumber,
                region = :region,
                town_village = :townVillage,
                membership_status = :membershipStatus,
                gender = :gender,
                conditions_precedent = :conditionsPrecedent,
                interest_rate = :interestRate,
                loan_type = :loanType,
                loan_status = :loanStatus,
                loan_status_comment = :loanStatusComment,
                loan_conditions = :loanConditions,
                approved_amount = :approvedAmount,
                disbursed_amount = :disbursedAmount,
                updated_at = :updatedAt
            WHERE id = :id
            """)
            .param("id", id)
            .param("projectDescription", form.projectDescription().trim())
            .param("applicantFirstName", form.applicantFirstName().trim())
            .param("applicantSurname", form.applicantSurname().trim())
            .param("applicantIdNumber", form.applicantIdNumber().trim())
            .param("contactNumber", form.contactNumber().trim())
            .param("region", form.region().trim())
            .param("townVillage", form.townVillage().trim())
            .param("membershipStatus", form.membershipStatus().trim())
            .param("gender", form.gender().trim())
            .param("conditionsPrecedent", normalizeOptional(form.conditionsPrecedent()))
            .param("interestRate", form.interestRate())
            .param("loanType", form.loanType().trim())
            .param("loanStatus", form.loanStatus().trim())
            .param("loanStatusComment", normalizeOptional(form.loanStatusComment()))
            .param("loanConditions", normalizeOptional(form.loanConditions()))
            .param("approvedAmount", form.approvedAmount())
            .param("disbursedAmount", form.disbursedAmount())
            .param("updatedAt", OffsetDateTime.now())
            .update();
    }

    private void validate(LoanForm form) {
        requireText(form.projectDescription(), "Project Description is required.");
        requireText(form.applicantFirstName(), "Applicant First Name is required.");
        requireText(form.applicantSurname(), "Applicant Surname is required.");
        requireText(form.applicantIdNumber(), "Applicant ID Number is required.");
        requireText(form.contactNumber(), "Contact Number is required.");
        requireText(form.region(), "Region is required.");
        requireText(form.townVillage(), "Town/Village is required.");
        requireText(form.membershipStatus(), "Membership Status is required.");
        requireText(form.gender(), "Gender is required.");
        requireText(form.loanType(), "Loan Type is required.");
        requireText(form.loanStatus(), "Loan Status is required.");

        if (form.interestRate() == null || form.interestRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest Rate must be zero or greater.");
        }

        if (form.approvedAmount() == null || form.approvedAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Approved Amount must be zero or greater.");
        }

        if (form.disbursedAmount() == null || form.disbursedAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Disbursed Amount must be zero or greater.");
        }

        if (form.disbursedAmount().compareTo(form.approvedAmount()) > 0) {
            throw new IllegalArgumentException("Disbursed Amount cannot exceed Approved Amount.");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private String normalizeOptional(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
