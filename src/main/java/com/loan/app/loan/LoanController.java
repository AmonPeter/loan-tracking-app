package com.loan.app.loan;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoanController {

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
    private static final List<String> LOAN_STATUSES = List.of(
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

    @ModelAttribute("loanStatuses")
    public List<String> loanStatuses() {
        return LOAN_STATUSES;
    }

    @ModelAttribute("membershipOptions")
    public List<String> membershipOptions() {
        return MEMBERSHIP_OPTIONS;
    }

    @GetMapping("/loans")
    public String loans(Model model) {
        model.addAttribute("loans", loanService.findAll());
        return "loans";
    }

    @GetMapping("/loans/create")
    public String createLoanPage() {
        return "loan-create";
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
        @RequestParam(required = false) String conditionsPrecedent,
        @RequestParam BigDecimal interestRate,
        @RequestParam String loanType,
        @RequestParam String loanStatus,
        @RequestParam(required = false) String loanStatusComment,
        @RequestParam(required = false) String loanConditions,
        @RequestParam BigDecimal approvedAmount,
        @RequestParam BigDecimal disbursedAmount,
        RedirectAttributes redirectAttributes
    ) {
        try {
            loanService.create(new LoanForm(
                projectDescription, applicantFirstName, applicantSurname, applicantIdNumber,
                contactNumber, region, townVillage, membershipStatus, gender, conditionsPrecedent,
                interestRate, loanType, loanStatus, loanStatusComment, loanConditions,
                approvedAmount, disbursedAmount
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
        @RequestParam String loanStatus,
        @RequestParam(required = false) String loanStatusComment,
        @RequestParam(required = false) String loanConditions,
        @RequestParam BigDecimal approvedAmount,
        @RequestParam BigDecimal disbursedAmount,
        RedirectAttributes redirectAttributes
    ) {
        try {
            loanService.update(id, new LoanForm(
                projectDescription, applicantFirstName, applicantSurname, applicantIdNumber,
                contactNumber, region, townVillage, membershipStatus, gender, conditionsPrecedent,
                interestRate, loanType, loanStatus, loanStatusComment, loanConditions,
                approvedAmount, disbursedAmount
            ));
            redirectAttributes.addFlashAttribute("successMessage", "Loan updated.");
        } catch (IllegalArgumentException | DataAccessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/loans";
    }
}
