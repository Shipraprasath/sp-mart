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
                            jakarta.servlet.http.HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        try {
            com.spmart.entity.Cart cart = cartService.addToCart(user, dto.getProductId(), dto.getQuantity());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Added to cart! Total items: " + cart.getTotalQuantity() + ". Keep adding or open your Cart when ready.");
        } catch (InsufficientStockException | BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:" + resolveSafeReturnPath(request, "/products");
    }

    @PostMapping(value = "/add", headers = "X-Requested-With=XMLHttpRequest")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> addToCartAjax(
            @Valid @ModelAttribute AddToCartDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userService.findById(userDetails.getId());
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        try {
            com.spmart.entity.Cart cart = cartService.addToCart(user, dto.getProductId(), dto.getQuantity());
            body.put("success", true);
            body.put("message", "Added to cart!");
            body.put("cartItemCount", cart.getTotalQuantity());
            body.put("productId", dto.getProductId());
            body.put("productQuantity", extractProductQuantity(cart, dto.getProductId()));
            return org.springframework.http.ResponseEntity.ok(body);
        } catch (InsufficientStockException | BusinessRuleException ex) {
            body.put("success", false);
            body.put("message", ex.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(body);
        }
    }

    @PostMapping("/buy-now")
    public String buyNow(@Valid @ModelAttribute AddToCartDto dto,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         jakarta.servlet.http.HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        try {
            cartService.addToCart(user, dto.getProductId(), dto.getQuantity());
            return "redirect:/checkout";
        } catch (InsufficientStockException | BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:" + resolveSafeReturnPath(request, "/products/" + dto.getProductId());
        }
    }

    private int extractProductQuantity(com.spmart.entity.Cart cart, Long productId) {
        if (cart == null || cart.getItems() == null || productId == null) {
            return 0;
        }
        for (com.spmart.entity.CartItem item : cart.getItems()) {
            if (item.getProduct() != null && productId.equals(item.getProduct().getId())) {
                return item.getQuantity() != null ? item.getQuantity() : 0;
            }
        }
        return 0;
    }

    private String resolveSafeReturnPath(jakarta.servlet.http.HttpServletRequest request, String fallbackPath) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return fallbackPath;
        }
        try {
            java.net.URI uri = java.net.URI.create(referer.trim());
            if (uri.getHost() != null && !uri.getHost().equalsIgnoreCase(request.getServerName())) {
                return fallbackPath;
            }
            String path = uri.getRawPath();
            if (path == null || !path.startsWith("/") || path.startsWith("//") || path.startsWith("/cart")) {
                return fallbackPath;
            }
            String query = uri.getRawQuery();
            return (query != null && !query.isBlank()) ? path + "?" + query : path;
        } catch (IllegalArgumentException ex) {
            return fallbackPath;
        }
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
