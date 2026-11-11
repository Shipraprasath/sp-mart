package com.spmart.controller;

import com.spmart.entity.User;
import com.spmart.exception.BusinessRuleException;
import com.spmart.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final UserService userService;

    public AdminCustomerController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listCustomers(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Page<User> customerPage = userService.getAllUsers(PageRequest.of(page, 15));
        model.addAttribute("customerPage", customerPage);
        return "admin/customers";
    }

    @PostMapping("/{id}/toggle")
    public String toggleCustomerStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Customer " + user.getEmail() + " status is now " + (user.isEnabled() ? "Active" : "Disabled") + ".");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/customers";
    }
}
