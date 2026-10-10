package com.spmart.controller;

import com.spmart.dto.ReviewDto;
import com.spmart.entity.User;
import com.spmart.exception.BusinessRuleException;
import com.spmart.security.CustomUserDetails;
import com.spmart.service.ReviewService;
import com.spmart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @PostMapping("/products/{productId}/review")
    public String submitReview(@PathVariable Long productId,
                               @Valid @ModelAttribute("reviewDto") ReviewDto reviewDto,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid rating (1-5) and review text.");
            return "redirect:/products/" + productId;
        }

        User user = userService.findById(userDetails.getId());
        reviewDto.setProductId(productId);

        try {
            reviewService.addReview(user, reviewDto);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you! Your review has been submitted.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/products/" + productId;
    }
}
