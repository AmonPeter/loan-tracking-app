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
        model.addAttribute("currentInterestRate", interestRateService.currentRate().orElse(null));
        model.addAttribute("interestRateHistory", interestRateService.recentRates());
        return "admin-interest-rate";
    }

    @PostMapping("/admin/interest-rate")
    public String updateInterestRate(
        @RequestParam BigDecimal interestRate,
        RedirectAttributes redirectAttributes
    ) {
        try {
            interestRateService.saveRate(interestRate);
            redirectAttributes.addFlashAttribute("successMessage", "Interest rate updated for future loan applications.");
        } catch (IllegalArgumentException | DataAccessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/interest-rate";
    }
}
