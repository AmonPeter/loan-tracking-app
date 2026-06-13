package com.loan.app.user;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountController {

    private final UserProfileService userProfileService;

    public AccountController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/account/password")
    public String password() {
        return "account-password";
    }

    @PostMapping("/account/password")
    public String changePassword(
        @RequestParam String currentPassword,
        @RequestParam String newPassword,
        @RequestParam String confirmPassword,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            userProfileService.changePassword(principal.getName(), currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/account/password";
    }
}
