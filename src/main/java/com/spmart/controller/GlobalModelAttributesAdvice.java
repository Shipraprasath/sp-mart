package com.spmart.controller;

import com.spmart.entity.Category;
import com.spmart.entity.User;
import com.spmart.security.CustomUserDetails;
import com.spmart.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalModelAttributesAdvice {

    private final CategoryService categoryService;
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final UserService userService;
    private final SellerService sellerService;

    public GlobalModelAttributesAdvice(CategoryService categoryService,
                                       CartService cartService,
                                       WishlistService wishlistService,
                                       UserService userService,
                                       SellerService sellerService) {
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.userService = userService;
        this.sellerService = sellerService;
    }

    @ModelAttribute
    public void resolveCsrfToken(jakarta.servlet.http.HttpServletRequest request) {
        org.springframework.security.web.csrf.CsrfToken csrfToken = 
                (org.springframework.security.web.csrf.CsrfToken) request.getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
    }

    @ModelAttribute("navbarCategories")
    public List<Category> getNavbarCategories() {
        return categoryService.getAllActiveCategories();
    }

    @ModelAttribute("cartItemCount")
    public int getCartItemCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return 0;
        }
        try {
            User user = userService.findById(userDetails.getId());
            return cartService.getOrCreateCart(user).getTotalQuantity();
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("wishlistItemCount")
    public long getWishlistItemCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return 0;
        }
        try {
            User user = userService.findById(userDetails.getId());
            return wishlistService.getWishlistCount(user);
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        try {
            return userService.findById(userDetails.getId());
        } catch (Exception e) {
            return null;
        }
    }

    @ModelAttribute("isApprovedSeller")
    public boolean isApprovedSeller(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        try {
            User user = userService.findById(userDetails.getId());
            return user.isSeller() && sellerService.isApprovedSeller(user);
        } catch (Exception e) {
            return false;
        }
    }
}
