package com.spmart.controller;

import com.spmart.entity.User;
import com.spmart.exception.BusinessRuleException;
import com.spmart.service.PasswordResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        try {
            String token = passwordResetService.createPasswordResetToken(email);
            // In development / academic demo, provide direct reset link for convenience:
            redirectAttributes.addFlashAttribute("successMessage",
                    "Password reset link generated! For demo purposes: <a href='/reset-password?token=" + token + "' style='font-weight:700; text-decoration:underline;'>Click here to reset your password</a>");
            return "redirect:/forgot-password";
        } catch (BusinessRuleException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        Optional<User> userOpt = passwordResetService.validatePasswordResetToken(token);
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "The reset token is invalid or has expired.");
            return "auth/forgot-password";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("errorMessage", "Password must be at least 6 characters.");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        try {
            passwordResetService.resetPassword(token, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password has been reset successfully! Please sign in.");
            return "redirect:/login";
        } catch (BusinessRuleException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
    }
}
