package com.loan.app.loan;

import java.math.BigDecimal;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoanTypeController {
    private final LoanTypeService loanTypeService;

    public LoanTypeController(LoanTypeService loanTypeService) {
        this.loanTypeService = loanTypeService;
    }

    @GetMapping("/admin/loan-types")
    public String loanTypes(Model model) {
        model.addAttribute("loanTypes", loanTypeService.findAll());
        return "admin-loan-types";
    }

    @PostMapping("/admin/loan-types")
    public String createLoanType(
        @RequestParam String name,
        @RequestParam BigDecimal interestRate,
        RedirectAttributes redirectAttributes
    ) {
        try {
            loanTypeService.create(name, interestRate);
            redirectAttributes.addFlashAttribute("successMessage", "Loan type created.");
        } catch (IllegalArgumentException | DataAccessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/loan-types";
    }

    @PostMapping("/admin/loan-types/{id}")
    public String updateLoanType(
        @PathVariable long id,
        @RequestParam String name,
        @RequestParam BigDecimal interestRate,
        @RequestParam(defaultValue = "false") boolean active,
        RedirectAttributes redirectAttributes
    ) {
        try {
            loanTypeService.update(id, name, interestRate, active);
            redirectAttributes.addFlashAttribute("successMessage", "Loan type updated.");
        } catch (IllegalArgumentException | DataAccessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/loan-types";
    }
}
