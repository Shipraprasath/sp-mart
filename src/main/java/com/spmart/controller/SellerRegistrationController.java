package com.spmart.controller;

import com.spmart.dto.SellerRegistrationDto;
import com.spmart.exception.BusinessRuleException;
import com.spmart.service.SellerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/seller")
public class SellerRegistrationController {

    private final SellerService sellerService;

    public SellerRegistrationController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("sellerDto")) {
            model.addAttribute("sellerDto", new SellerRegistrationDto());
        }
        return "seller/register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("sellerDto") SellerRegistrationDto dto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            return "seller/register";
        }

        if (!dto.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "error.sellerDto", "Passwords do not match");
            return "seller/register";
        }

        try {
            sellerService.registerSeller(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Your seller application has been submitted successfully! An administrator will review and approve your store.");
            return "redirect:/seller/pending";
        } catch (BusinessRuleException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "seller/register";
        }
    }

    @GetMapping("/pending")
    public String pendingApprovalPage() {
        return "seller/pending";
    }
}
