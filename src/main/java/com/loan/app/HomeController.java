package com.loan.app;

import com.loan.app.loan.LoanService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private static final List<String> LOAN_STATUSES = List.of(
        "Initiation",
        "Approved",
        "Withdrawn",
        "Declined"
    );
    private static final List<String> LOAN_HEALTH_GROUPS = List.of(
        "Active Loan",
        "Early Arrears",
        "Persistent Arrears",
        "Formal Default",
        "Enforcement"
    );

    private final LoanService loanService;

    public HomeController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalDisbursedAmount", loanService.totalDisbursedAmount());
        model.addAttribute("totalApprovedAmount", loanService.totalApprovedAmount());
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        LOAN_STATUSES.forEach(status -> statusCounts.put(status, 0L));
        loanService.totalLoansByStatus().forEach(statusCounts::put);
        model.addAttribute("totalLoansByStatus", statusCounts.entrySet());
        Map<String, Long> healthCounts = new LinkedHashMap<>();
        LOAN_HEALTH_GROUPS.forEach(group -> healthCounts.put(group, 0L));
        loanService.loanHealthCounts().forEach(healthCounts::put);
        model.addAttribute("loanHealthCounts", healthCounts.entrySet());
        return "home";
    }
}
