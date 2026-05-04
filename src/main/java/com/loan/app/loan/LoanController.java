package com.loan.app.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.Year;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.dao.DataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;

@Controller
public class LoanController {
    private static final int LOANS_PAGE_SIZE = 10;

    private static final List<String> NAMIBIA_REGIONS = List.of(
        "Erongo",
        "Hardap",
        "Karas",
        "Kavango East",
        "Kavango West",
        "Khomas",
        "Kunene",
        "Ohangwena",
        "Omaheke",
        "Omusati",
        "Oshana",
        "Oshikoto",
        "Otjozondjupa",
        "Zambezi"
    );
    private static final List<String> GENDERS = List.of("Male", "Female");
    private static final List<String> LOAN_TYPES = List.of(
        "Micro loan",
        "Education Support loan",
        "SME loan",
        "Not Stated"
    );
    private static final List<Integer> LOAN_DURATIONS = List.of(12, 36);
    private static final List<Integer> GRACE_PERIOD_DAYS = List.of(30, 60, 90, 120, 160, 180, 210);
    private static final List<String> LOAN_STATUSES = List.of(
        "Initiation",
        "Approved",
        "Withdrawn",
        "Declined"
    );
    private static final List<String> MEMBERSHIP_OPTIONS = List.of(
        "Dependent",
        "Member",
        "Registered Ex Member"
    );

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @ModelAttribute("regions")
    public List<String> regions() {
        return NAMIBIA_REGIONS;
    }

    @ModelAttribute("genders")
    public List<String> genders() {
        return GENDERS;
    }

    @ModelAttribute("loanTypes")
    public List<String> loanTypes() {
        return LOAN_TYPES;
    }

    @ModelAttribute("loanDurations")
    public List<Integer> loanDurations() {
        return LOAN_DURATIONS;
    }

    @ModelAttribute("gracePeriodDaysOptions")
    public List<Integer> gracePeriodDaysOptions() {
        return GRACE_PERIOD_DAYS;
    }

    @ModelAttribute("loanStatuses")
    public List<String> loanStatuses() {
        return LOAN_STATUSES;
    }

    @ModelAttribute("repaymentStartMonths")
    public List<Integer> repaymentStartMonths() {
        return IntStream.rangeClosed(1, 12).boxed().toList();
    }

    @ModelAttribute("repaymentStartYears")
    public List<Integer> repaymentStartYears() {
        int currentYear = Year.now().getValue();
        return IntStream.rangeClosed(currentYear, currentYear + 15).boxed().toList();
    }

    @ModelAttribute("repaymentStartDates")
    public List<Integer> repaymentStartDates() {
        return List.of(1, 15);
    }

    @ModelAttribute("membershipOptions")
    public List<String> membershipOptions() {
        return MEMBERSHIP_OPTIONS;
    }

    @GetMapping("/loans")
    public String loans(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String q, Model model) {
        long totalLoans = loanService.countAll(q);
        int totalPages = (int) Math.max(1, Math.ceil((double) totalLoans / LOANS_PAGE_SIZE));
        int currentPage = Math.min(Math.max(page, 0), totalPages - 1);

        model.addAttribute("loans", loanService.findPage(currentPage, LOANS_PAGE_SIZE, q));
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumbers", IntStream.range(0, totalPages).boxed().toList());
        model.addAttribute("hasPrevious", currentPage > 0);
        model.addAttribute("hasNext", currentPage < totalPages - 1);
        model.addAttribute("q", q == null ? "" : q.trim());
        return "loans";
    }

    @GetMapping("/loans/create")
    public String createLoanPage() {
        return "loan-create";
    }

    @GetMapping("/loans/{id}")
    public String loanDetail(@PathVariable long id, Model model) {
        LoanView loan = loanService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        model.addAttribute("loan", loan);
        model.addAttribute("repayments", loanService.findRepaymentsByLoanId(id));
        model.addAttribute("outstandingAmount", loanService.outstandingAmount(loan));
        return "loan-detail";
    }

    @GetMapping("/reports")
    public String reports(
        @RequestParam(required = false) Integer fromMonth,
        @RequestParam(required = false) Integer fromYear,
        @RequestParam(required = false) Integer toMonth,
        @RequestParam(required = false) Integer toYear,
        @RequestParam(required = false) String loanType,
        @RequestParam(required = false) String region,
        Model model
    ) {
        LocalDate today = LocalDate.now();
        int selectedFromMonth = fromMonth == null ? today.getMonthValue() : fromMonth;
        int selectedFromYear = fromYear == null ? today.getYear() : fromYear;
        int selectedToMonth = toMonth == null ? today.getMonthValue() : toMonth;
        int selectedToYear = toYear == null ? today.getYear() : toYear;
        LocalDate selectedFromDate = LocalDate.of(selectedFromYear, selectedFromMonth, 1);
        LocalDate selectedToDate = YearMonth.of(selectedToYear, selectedToMonth).atEndOfMonth();

        model.addAttribute("reportMonths", IntStream.rangeClosed(1, 12).boxed().toList());
        model.addAttribute("reportYears", IntStream.rangeClosed(today.getYear() - 5, today.getYear() + 10).boxed().toList());
        model.addAttribute("selectedFromMonth", selectedFromMonth);
        model.addAttribute("selectedFromYear", selectedFromYear);
        model.addAttribute("selectedToMonth", selectedToMonth);
        model.addAttribute("selectedToYear", selectedToYear);
        model.addAttribute("selectedLoanType", loanType == null ? "" : loanType);
        model.addAttribute("selectedRegion", region == null ? "" : region);
        model.addAttribute("rows", loanService.reportRows(selectedFromDate, selectedToDate, loanType, region));
        return "reports";
    }

    @PostMapping("/loans/create")
    public String createLoan(
        @RequestParam String projectDescription,
        @RequestParam String applicantFirstName,
        @RequestParam String applicantSurname,
        @RequestParam String applicantIdNumber,
        @RequestParam String contactNumber,
        @RequestParam String region,
        @RequestParam String townVillage,
        @RequestParam String membershipStatus,
        @RequestParam String gender,
        @RequestParam String loanType,
        @RequestParam Integer durationMonths,
        RedirectAttributes redirectAttributes
    ) {
        try {
            loanService.create(new LoanForm(
                projectDescription, applicantFirstName, applicantSurname, applicantIdNumber,
                contactNumber, region, townVillage, membershipStatus, gender, null,
                BigDecimal.ZERO, loanType, durationMonths, 30, "Initiation", null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, null, null, null
            ));
            redirectAttributes.addFlashAttribute("successMessage", "Loan created.");
        } catch (IllegalArgumentException | DataAccessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/loans/create";
        }
        return "redirect:/loans";
    }

    @PostMapping("/loans/{id}")
    public String updateLoan(
        @PathVariable long id,
        @RequestParam String projectDescription,
        @RequestParam String applicantFirstName,
        @RequestParam String applicantSurname,
        @RequestParam String applicantIdNumber,
        @RequestParam String contactNumber,
        @RequestParam String region,
        @RequestParam String townVillage,
        @RequestParam String membershipStatus,
        @RequestParam String gender,
        @RequestParam(required = false) String conditionsPrecedent,
        @RequestParam BigDecimal interestRate,
        @RequestParam String loanType,
        @RequestParam Integer durationMonths,
        @RequestParam Integer gracePeriodDays,
        @RequestParam String loanStatus,
        @RequestParam(required = false) String loanStatusComment,
        @RequestParam(required = false) String loanConditions,
        @RequestParam BigDecimal approvedAmount,
        @RequestParam BigDecimal disbursedAmount,
        @RequestParam(required = false) Integer repaymentStartMonth,
        @RequestParam(required = false) Integer repaymentStartYear,
        @RequestParam(required = false) Integer repaymentStartDate,
        RedirectAttributes redirectAttributes
    ) {
        try {
            loanService.update(id, new LoanForm(
                projectDescription, applicantFirstName, applicantSurname, applicantIdNumber,
                contactNumber, region, townVillage, membershipStatus, gender, conditionsPrecedent,
                interestRate, loanType, durationMonths, gracePeriodDays, loanStatus, loanStatusComment, loanConditions,
                approvedAmount, disbursedAmount, repaymentStartMonth, repaymentStartYear, repaymentStartDate
            ));
            redirectAttributes.addFlashAttribute("successMessage", "Loan updated.");
        } catch (IllegalArgumentException | DataAccessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/loans/" + id;
        }
        return "redirect:/loans/" + id;
    }

    @PostMapping("/loans/{id}/payments")
    public String captureRepayment(
        @PathVariable long id,
        @RequestParam BigDecimal paymentAmount,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
        @RequestParam(defaultValue = "false") boolean useLatestDueMonth,
        @RequestParam(required = false) Integer repaymentMonth,
        @RequestParam(required = false) Integer repaymentYear,
        @RequestParam(required = false) String paymentNote,
        RedirectAttributes redirectAttributes
    ) {
        try {
            loanService.captureRepayment(
                id,
                paymentAmount,
                paymentDate == null ? LocalDate.now() : paymentDate,
                useLatestDueMonth,
                repaymentMonth,
                repaymentYear,
                paymentNote
            );
            redirectAttributes.addFlashAttribute("successMessage", "Repayment captured.");
        } catch (IllegalArgumentException | DataAccessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/loans/" + id;
    }
}
