package com.loan.app.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminUserController {

    private final UserAdminService userAdminService;

    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userAdminService.findAllUsers());
        return "admin-users";
    }

    @PostMapping("/admin/users")
    public String createUser(
        @RequestParam String email,
        @RequestParam String password,
        @RequestParam(defaultValue = "USER") String role,
        @RequestParam(defaultValue = "false") boolean enabled,
        RedirectAttributes redirectAttributes
    ) {
        try {
            userAdminService.createUser(email, password, role, enabled);
            redirectAttributes.addFlashAttribute("successMessage", "User created.");
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}")
    public String updateUser(
        @PathVariable long id,
        @RequestParam(defaultValue = "USER") String role,
        @RequestParam(defaultValue = "false") boolean enabled,
        @RequestParam(required = false) String password,
        RedirectAttributes redirectAttributes
    ) {
        try {
            userAdminService.updateUser(id, role, enabled, password);
            redirectAttributes.addFlashAttribute("successMessage", "User updated.");
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/users";
    }
}
