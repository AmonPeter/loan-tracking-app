package com.loan.app.loan;

import java.math.BigDecimal;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class InterestRateController {
    private final InterestRateService interestRateService;

    public InterestRateController(InterestRateService interestRateService) {
        this.interestRateService = interestRateService;
    }

    @GetMapping("/admin/interest-rate")
    public String interestRate(Model model) {
        model.addAttribute("loanTypes", interestRateService.activeLoanTypeRates());
        return "admin-interest-rate";
    }

    @PostMapping("/admin/interest-rate")
    public String updateInterestRate(
        @RequestParam long loanTypeId,
        @RequestParam BigDecimal interestRate,
        RedirectAttributes redirectAttributes
    ) {
        try {
            interestRateService.saveRate(loanTypeId, interestRate);
            redirectAttributes.addFlashAttribute("successMessage", "Loan type interest rate updated for future applications.");
        } catch (IllegalArgumentException | DataAccessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/interest-rate";
    }
}
