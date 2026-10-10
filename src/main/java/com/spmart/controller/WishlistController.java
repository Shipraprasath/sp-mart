package com.spmart.controller;

import com.spmart.entity.User;
import com.spmart.entity.WishlistItem;
import com.spmart.exception.BusinessRuleException;
import com.spmart.security.CustomUserDetails;
import com.spmart.service.UserService;
import com.spmart.service.WishlistService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserService userService;

    public WishlistController(WishlistService wishlistService, UserService userService) {
        this.wishlistService = wishlistService;
        this.userService = userService;
    }

    @GetMapping
    public String viewWishlist(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId());
        List<WishlistItem> wishlist = wishlistService.getUserWishlist(user);
        model.addAttribute("wishlist", wishlist);
        return "account/wishlist";
    }

    @PostMapping("/add/{productId}")
    public String addToWishlist(@PathVariable Long productId,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        wishlistService.addToWishlist(user, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Item saved to your wishlist!");
        return "redirect:/wishlist";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromWishlist(@PathVariable Long productId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        wishlistService.removeFromWishlist(user, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from wishlist.");
        return "redirect:/wishlist";
    }

    @PostMapping("/move-to-cart/{productId}")
    public String moveToCart(@PathVariable Long productId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        try {
            wishlistService.moveToCart(user, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Item moved to your cart!");
            return "redirect:/cart";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/wishlist";
        }
    }
}
