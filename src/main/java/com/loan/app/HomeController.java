package com.loan.app;

import com.loan.app.loan.LoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final LoanService loanService;

    public HomeController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalDisbursedAmount", loanService.totalDisbursedAmount());
        model.addAttribute("totalApprovedAmount", loanService.totalApprovedAmount());
        return "home";
    }
}
