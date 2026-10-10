package com.spmart.controller;

import com.spmart.dto.AddToCartDto;
import com.spmart.dto.CartItemUpdateDto;
import com.spmart.entity.Cart;
import com.spmart.entity.User;
import com.spmart.exception.BusinessRuleException;
import com.spmart.exception.InsufficientStockException;
import com.spmart.security.CustomUserDetails;
import com.spmart.service.CartService;
import com.spmart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId());
        Cart cart = cartService.getOrCreateCart(user);

        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal shippingFee = cartService.calculateShippingFee(subtotal);
        BigDecimal finalTotal = subtotal.add(shippingFee);

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("finalTotal", finalTotal);
        model.addAttribute("freeShippingThreshold", cartService.getFreeShippingThreshold());

        return "cart/view";
    }

    @PostMapping("/add")
    public String addToCart(@Valid @ModelAttribute AddToCartDto dto,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        try {
            cartService.addToCart(user, dto.getProductId(), dto.getQuantity());
            redirectAttributes.addFlashAttribute("successMessage", "Item successfully added to your shopping cart!");
        } catch (InsufficientStockException | BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@Valid @ModelAttribute CartItemUpdateDto dto,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        try {
            cartService.updateQuantity(user, dto.getCartItemId(), dto.getQuantity());
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated successfully.");
        } catch (InsufficientStockException | BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove/{itemId}")
    public String removeItem(@PathVariable Long itemId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        cartService.removeFromCart(user, itemId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart.");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        cartService.clearCart(user);
        redirectAttributes.addFlashAttribute("successMessage", "Cart has been cleared.");
        return "redirect:/cart";
    }
}
