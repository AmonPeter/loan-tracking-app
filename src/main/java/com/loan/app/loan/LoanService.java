package com.loan.app.loan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class LoanService {
    private static final Set<Integer> ALLOWED_GRACE_PERIOD_DAYS = Set.of(30, 60, 90, 120, 160, 180, 210);

    private final JdbcClient jdbcClient;

    private record RepaymentPeriod(int month, int year) {
    }

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
                duration_months AS durationMonths,
                grace_period_days AS gracePeriodDays,
                loan_status AS loanStatus,
                loan_status_comment AS loanStatusComment,
                loan_conditions AS loanConditions,
                approved_amount AS approvedAmount,
                disbursed_amount AS disbursedAmount,
                repayment_start_month AS repaymentStartMonth,
                repayment_start_year AS repaymentStartYear,
                repayment_start_date AS repaymentStartDate
            FROM loans
            ORDER BY created_at DESC
            """)
            .query(LoanView.class)
            .list();
    }

    public List<LoanView> findPage(int page, int pageSize) {
        return findPage(page, pageSize, null);
    }

    public List<LoanView> findPage(int page, int pageSize, String searchTerm) {
        int safePage = Math.max(page, 0);
        int safePageSize = Math.max(pageSize, 1);
        int offset = safePage * safePageSize;
        String query = normalizeSearch(searchTerm);

        if (query != null) {
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
                    duration_months AS durationMonths,
                    grace_period_days AS gracePeriodDays,
                    loan_status AS loanStatus,
                    loan_status_comment AS loanStatusComment,
                    loan_conditions AS loanConditions,
                    approved_amount AS approvedAmount,
                    disbursed_amount AS disbursedAmount,
                    repayment_start_month AS repaymentStartMonth,
                    repayment_start_year AS repaymentStartYear,
                    repayment_start_date AS repaymentStartDate
                FROM loans
                WHERE lower(applicant_first_name) LIKE :query
                   OR lower(applicant_surname) LIKE :query
                ORDER BY created_at DESC
                LIMIT :limit OFFSET :offset
                """)
                .param("query", query)
                .param("limit", safePageSize)
                .param("offset", offset)
                .query(LoanView.class)
                .list();
        }

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
                duration_months AS durationMonths,
                grace_period_days AS gracePeriodDays,
                loan_status AS loanStatus,
                loan_status_comment AS loanStatusComment,
                loan_conditions AS loanConditions,
                approved_amount AS approvedAmount,
                disbursed_amount AS disbursedAmount,
                repayment_start_month AS repaymentStartMonth,
                repayment_start_year AS repaymentStartYear,
                repayment_start_date AS repaymentStartDate
            FROM loans
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """)
            .param("limit", safePageSize)
            .param("offset", offset)
            .query(LoanView.class)
            .list();
    }

    public long countAll() {
        return countAll(null);
    }

    public BigDecimal totalDisbursedAmount() {
        BigDecimal total = jdbcClient.sql("""
            SELECT COALESCE(SUM(disbursed_amount), 0)
            FROM loans
            WHERE loan_status = 'Approved'
            """)
            .query(BigDecimal.class)
            .single();
        return total == null ? BigDecimal.ZERO : total;
    }

    public BigDecimal totalApprovedAmount() {
        BigDecimal total = jdbcClient.sql("""
            SELECT COALESCE(SUM(approved_amount), 0)
            FROM loans
            WHERE loan_status = 'Approved'
            """)
            .query(BigDecimal.class)
            .single();
        return total == null ? BigDecimal.ZERO : total;
    }

    public Map<String, Long> totalLoansByStatus() {
        Map<String, Long> counts = new LinkedHashMap<>();
        jdbcClient.sql("""
            SELECT loan_status AS loanStatus, COUNT(*) AS total
            FROM loans
            GROUP BY loan_status
            """)
            .query()
            .listOfRows()
            .forEach(row -> {
                String status = (String) row.get("loanStatus");
                Number total = (Number) row.get("total");
                counts.put(status, total == null ? 0L : total.longValue());
            });
        return counts;
    }

    public Map<String, Long> loanHealthCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        Map<Long, BigDecimal> paidByLoanId = new LinkedHashMap<>();
        jdbcClient.sql("""
            SELECT loan_id AS loanId, COALESCE(SUM(payment_amount), 0) AS totalPaid
            FROM loan_repayments
            GROUP BY loan_id
            """)
            .query()
            .listOfRows()
            .forEach(row -> {
                Number loanId = (Number) row.get("loanId");
                BigDecimal totalPaid = (BigDecimal) row.get("totalPaid");
                if (loanId != null) {
                    paidByLoanId.put(loanId.longValue(), totalPaid == null ? BigDecimal.ZERO : totalPaid);
                }
            });

        List<LoanView> approvedLoans = jdbcClient.sql("""
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
                duration_months AS durationMonths,
                grace_period_days AS gracePeriodDays,
                loan_status AS loanStatus,
                loan_status_comment AS loanStatusComment,
                loan_conditions AS loanConditions,
                approved_amount AS approvedAmount,
                disbursed_amount AS disbursedAmount,
                repayment_start_month AS repaymentStartMonth,
                repayment_start_year AS repaymentStartYear,
                repayment_start_date AS repaymentStartDate
            FROM loans
            WHERE loan_status = 'Approved'
              AND repayment_start_month IS NOT NULL
              AND repayment_start_year IS NOT NULL
              AND repayment_start_date IS NOT NULL
            """)
            .query(LoanView.class)
            .list();

        for (LoanView loan : approvedLoans) {
            String health = classifyLoanHealth(loan, paidByLoanId.getOrDefault(loan.id(), BigDecimal.ZERO), today);
            counts.merge(health, 1L, Long::sum);
        }

        return counts;
    }

    public long countAll(String searchTerm) {
        String query = normalizeSearch(searchTerm);
        if (query != null) {
            Long filteredCount = jdbcClient.sql("""
                SELECT COUNT(*)
                FROM loans
                WHERE lower(applicant_first_name) LIKE :query
                   OR lower(applicant_surname) LIKE :query
                """)
                .param("query", query)
                .query(Long.class)
                .single();
            return filteredCount == null ? 0L : filteredCount;
        }

        Long count = jdbcClient.sql("SELECT COUNT(*) FROM loans")
            .query(Long.class)
            .single();
        return count == null ? 0L : count;
    }

    public Optional<LoanView> findById(long id) {
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
                duration_months AS durationMonths,
                grace_period_days AS gracePeriodDays,
                loan_status AS loanStatus,
                loan_status_comment AS loanStatusComment,
                loan_conditions AS loanConditions,
                approved_amount AS approvedAmount,
                disbursed_amount AS disbursedAmount,
                repayment_start_month AS repaymentStartMonth,
                repayment_start_year AS repaymentStartYear,
                repayment_start_date AS repaymentStartDate
            FROM loans
            WHERE id = :id
            """)
            .param("id", id)
            .query(LoanView.class)
            .optional();
    }

    public void create(LoanForm form) {
        validate(form, false);
        OffsetDateTime now = OffsetDateTime.now();

        jdbcClient.sql("""
            INSERT INTO loans (
                project_description, applicant_first_name, applicant_surname,
                applicant_id_number, contact_number, region, town_village,
                membership_status, gender, conditions_precedent, interest_rate,
                loan_type, duration_months, grace_period_days, loan_status, loan_status_comment, loan_conditions,
                approved_amount, disbursed_amount, repayment_start_month, repayment_start_year, repayment_start_date, created_at, updated_at
            ) VALUES (
                :projectDescription, :applicantFirstName, :applicantSurname,
                :applicantIdNumber, :contactNumber, :region, :townVillage,
                :membershipStatus, :gender, :conditionsPrecedent, :interestRate,
                :loanType, :durationMonths, :gracePeriodDays, :loanStatus, :loanStatusComment, :loanConditions,
                :approvedAmount, :disbursedAmount, :repaymentStartMonth, :repaymentStartYear, :repaymentStartDate, :createdAt, :updatedAt
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
            .param("durationMonths", form.durationMonths())
            .param("gracePeriodDays", form.gracePeriodDays())
            .param("loanStatus", "Initiation")
            .param("loanStatusComment", null)
            .param("loanConditions", normalizeOptional(form.loanConditions()))
            .param("approvedAmount", form.approvedAmount())
            .param("disbursedAmount", form.disbursedAmount())
            .param("repaymentStartMonth", form.repaymentStartMonth())
            .param("repaymentStartYear", form.repaymentStartYear())
            .param("repaymentStartDate", form.repaymentStartDate())
            .param("createdAt", now)
            .param("updatedAt", now)
            .update();
    }

    public void update(long id, LoanForm form) {
        validate(form, true);

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
                duration_months = :durationMonths,
                grace_period_days = :gracePeriodDays,
                loan_status = :loanStatus,
                loan_status_comment = :loanStatusComment,
                loan_conditions = :loanConditions,
                approved_amount = :approvedAmount,
                disbursed_amount = :disbursedAmount,
                repayment_start_month = :repaymentStartMonth,
                repayment_start_year = :repaymentStartYear,
                repayment_start_date = :repaymentStartDate,
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
            .param("durationMonths", form.durationMonths())
            .param("gracePeriodDays", form.gracePeriodDays())
            .param("loanStatus", form.loanStatus().trim())
            .param("loanStatusComment", normalizeOptional(form.loanStatusComment()))
            .param("loanConditions", normalizeOptional(form.loanConditions()))
            .param("approvedAmount", form.approvedAmount())
            .param("disbursedAmount", form.disbursedAmount())
            .param("repaymentStartMonth", form.repaymentStartMonth())
            .param("repaymentStartYear", form.repaymentStartYear())
            .param("repaymentStartDate", form.repaymentStartDate())
            .param("updatedAt", OffsetDateTime.now())
            .update();
    }

    public void captureRepayment(
        long loanId,
        BigDecimal paymentAmount,
        LocalDate paymentDate,
        boolean useLatestDueMonth,
        Integer repaymentMonth,
        Integer repaymentYear,
        String paymentNote
    ) {
        LoanView loan = findById(loanId).orElseThrow(() -> new IllegalArgumentException("Loan not found."));

        if (!"Approved".equalsIgnoreCase(loan.loanStatus())) {
            throw new IllegalArgumentException("Repayments can only be captured for Approved loans.");
        }
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment Amount must be greater than zero.");
        }
        if (paymentDate == null) {
            throw new IllegalArgumentException("Payment Date is required.");
        }
        if (loan.repaymentStartMonth() == null || loan.repaymentStartYear() == null || loan.repaymentStartDate() == null) {
            throw new IllegalArgumentException("Repayment start setup is required before capturing payments.");
        }

        RepaymentPeriod period = resolveRepaymentPeriod(
            loan,
            paymentDate,
            useLatestDueMonth,
            repaymentMonth,
            repaymentYear
        );

        jdbcClient.sql("""
            INSERT INTO loan_repayments (
                loan_id,
                payment_amount,
                payment_date,
                repayment_month,
                repayment_year,
                payment_note,
                created_at
            ) VALUES (
                :loanId,
                :paymentAmount,
                :paymentDate,
                :repaymentMonth,
                :repaymentYear,
                :paymentNote,
                :createdAt
            )
            """)
            .param("loanId", loanId)
            .param("paymentAmount", paymentAmount)
            .param("paymentDate", paymentDate)
            .param("repaymentMonth", period.month())
            .param("repaymentYear", period.year())
            .param("paymentNote", normalizeOptional(paymentNote))
            .param("createdAt", OffsetDateTime.now())
            .update();
    }

    private RepaymentPeriod resolveRepaymentPeriod(
        LoanView loan,
        LocalDate paymentDate,
        boolean useLatestDueMonth,
        Integer repaymentMonth,
        Integer repaymentYear
    ) {
        LocalDate firstDueDate = LocalDate.of(
            loan.repaymentStartYear(),
            loan.repaymentStartMonth(),
            loan.repaymentStartDate()
        ).plusDays(loan.gracePeriodDays());

        LocalDate lastDueDate = firstDueDate.plusMonths(Math.max(loan.durationMonths() - 1, 0));

        if (useLatestDueMonth) {
            if (paymentDate.isBefore(firstDueDate)) {
                throw new IllegalArgumentException("No repayment is due yet for this loan.");
            }
            int monthDiff = (paymentDate.getYear() - firstDueDate.getYear()) * 12 + (paymentDate.getMonthValue() - firstDueDate.getMonthValue());
            int dueCount = monthDiff + (paymentDate.getDayOfMonth() >= firstDueDate.getDayOfMonth() ? 1 : 0);
            dueCount = Math.max(1, Math.min(loan.durationMonths(), dueCount));
            LocalDate dueDate = firstDueDate.plusMonths(dueCount - 1L);
            return new RepaymentPeriod(dueDate.getMonthValue(), dueDate.getYear());
        }

        if (repaymentMonth == null || repaymentMonth < 1 || repaymentMonth > 12) {
            throw new IllegalArgumentException("Repayment Month is required.");
        }
        if (repaymentYear == null || repaymentYear < 2000 || repaymentYear > 2200) {
            throw new IllegalArgumentException("Repayment Year is required.");
        }

        LocalDate selectedPeriodDate = LocalDate.of(repaymentYear, repaymentMonth, 1);
        LocalDate firstPeriodDate = LocalDate.of(firstDueDate.getYear(), firstDueDate.getMonthValue(), 1);
        LocalDate lastPeriodDate = LocalDate.of(lastDueDate.getYear(), lastDueDate.getMonthValue(), 1);

        if (selectedPeriodDate.isBefore(firstPeriodDate) || selectedPeriodDate.isAfter(lastPeriodDate)) {
            throw new IllegalArgumentException("Selected repayment month/year is outside the loan repayment schedule.");
        }

        return new RepaymentPeriod(repaymentMonth, repaymentYear);
    }

    private void validate(LoanForm form, boolean requireStatus) {
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
        if (form.durationMonths() == null || (form.durationMonths() != 12 && form.durationMonths() != 36)) {
            throw new IllegalArgumentException("Duration must be 12 or 36 months.");
        }
        if (form.gracePeriodDays() == null || !ALLOWED_GRACE_PERIOD_DAYS.contains(form.gracePeriodDays())) {
            throw new IllegalArgumentException("Grace Period must be one of: 30, 60, 90, 120, 160, 180, 210.");
        }
        if (requireStatus) {
            requireText(form.loanStatus(), "Loan Status is required.");
        }

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

        if ("Approved".equalsIgnoreCase(form.loanStatus())) {
            if (form.repaymentStartMonth() == null || form.repaymentStartMonth() < 1 || form.repaymentStartMonth() > 12) {
                throw new IllegalArgumentException("Repayment Start Month is required when loan status is Approved.");
            }
            if (form.repaymentStartYear() == null || form.repaymentStartYear() < 2000 || form.repaymentStartYear() > 2200) {
                throw new IllegalArgumentException("Repayment Start Year is required when loan status is Approved.");
            }
            if (form.repaymentStartDate() == null || (form.repaymentStartDate() != 1 && form.repaymentStartDate() != 15)) {
                throw new IllegalArgumentException("Repayment Start Date must be either 1st or 15th when loan status is Approved.");
            }
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

    private String normalizeSearch(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private String classifyLoanHealth(LoanView loan, BigDecimal totalPaid, LocalDate today) {
        if (loan.disbursedAmount() == null || loan.interestRate() == null || loan.durationMonths() == null || loan.durationMonths() <= 0) {
            return "Active Loan";
        }

        BigDecimal principal = loan.disbursedAmount();
        BigDecimal annualRate = loan.interestRate().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal effectiveYears = BigDecimal.valueOf(loan.durationMonths() + (loan.gracePeriodDays() / 30.0))
            .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal totalInterest = principal.multiply(annualRate).multiply(effectiveYears);
        BigDecimal totalRepayable = principal.add(totalInterest);
        BigDecimal monthlyInstallment = totalRepayable.divide(BigDecimal.valueOf(loan.durationMonths()), 10, RoundingMode.HALF_UP);

        if (monthlyInstallment.compareTo(BigDecimal.ZERO) <= 0) {
            return "Active Loan";
        }

        LocalDate firstDueDate = LocalDate.of(loan.repaymentStartYear(), loan.repaymentStartMonth(), loan.repaymentStartDate())
            .plusDays(loan.gracePeriodDays());

        int coveredInstallments = totalPaid.divide(monthlyInstallment, 0, RoundingMode.DOWN).intValue();
        coveredInstallments = Math.max(0, Math.min(loan.durationMonths(), coveredInstallments));
        if (coveredInstallments >= loan.durationMonths()) {
            return "Active Loan";
        }

        LocalDate nextDueDate = firstDueDate.plusMonths(coveredInstallments);
        long daysBehind = today.isAfter(nextDueDate) ? ChronoUnit.DAYS.between(nextDueDate, today) : 0;
        if (daysBehind <= 0) {
            return "Active Loan";
        }

        int dueCount = 0;
        if (!today.isBefore(firstDueDate)) {
            int monthDiff = (today.getYear() - firstDueDate.getYear()) * 12 + (today.getMonthValue() - firstDueDate.getMonthValue());
            dueCount = monthDiff + (today.getDayOfMonth() >= firstDueDate.getDayOfMonth() ? 1 : 0);
            dueCount = Math.max(0, Math.min(loan.durationMonths(), dueCount));
        }
        int missedInstallments = Math.max(0, dueCount - coveredInstallments);

        if (daysBehind >= 90) {
            return "Enforcement";
        }
        if (daysBehind <= 30) {
            return "Early Arrears";
        }
        if (daysBehind <= 60) {
            return "Persistent Arrears";
        }
        if (missedInstallments >= 3) {
            return "Formal Default";
        }
        return "Persistent Arrears";
    }
}
