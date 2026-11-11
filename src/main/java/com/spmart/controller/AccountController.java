package com.spmart.controller;

import com.spmart.dto.ChangePasswordDto;
import com.spmart.dto.UserProfileDto;
import com.spmart.entity.CustomerOrder;
import com.spmart.entity.OrderStatusHistory;
import com.spmart.entity.User;
import com.spmart.exception.BusinessRuleException;
import com.spmart.security.CustomUserDetails;
import com.spmart.service.OrderService;
import com.spmart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserService userService;
    private final OrderService orderService;

    public AccountController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId());
        if (!model.containsAttribute("profileDto")) {
            model.addAttribute("profileDto", new UserProfileDto(user.getFullName(), user.getPhone()));
        }
        if (!model.containsAttribute("passwordDto")) {
            model.addAttribute("passwordDto", new ChangePasswordDto());
        }
        model.addAttribute("user", user);
        return "account/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profileDto") UserProfileDto profileDto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "account/profile";
        }
        User user = userService.findById(userDetails.getId());
        userService.updateProfile(user, profileDto);
        redirectAttributes.addFlashAttribute("profileSuccess", "Profile updated successfully.");
        return "redirect:/account/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordDto") ChangePasswordDto passwordDto,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "account/profile";
        }
        User user = userService.findById(userDetails.getId());
        try {
            userService.changePassword(user, passwordDto);
            redirectAttributes.addFlashAttribute("passwordSuccess", "Password updated successfully.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("passwordError", ex.getMessage());
        }
        return "redirect:/account/profile";
    }

    @GetMapping("/orders")
    public String orderHistory(@RequestParam(value = "page", defaultValue = "0") int page,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        User user = userService.findById(userDetails.getId());
        Page<CustomerOrder> orderPage = orderService.getUserOrders(user, PageRequest.of(page, 10));
        model.addAttribute("orderPage", orderPage);
        return "account/orders";
    }

    @GetMapping("/orders/{orderNumber}")
    public String orderDetails(@PathVariable String orderNumber,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        User user = userService.findById(userDetails.getId());
        CustomerOrder order = orderService.getOrderByNumberAndUser(orderNumber, user);
        List<OrderStatusHistory> history = orderService.getOrderHistory(order);

        model.addAttribute("order", order);
        model.addAttribute("statusHistory", history);
        return "account/order-details";
    }

    @PostMapping("/orders/{orderNumber}/cancel")
    public String cancelOrder(@PathVariable String orderNumber,
                              @RequestParam(value = "reason", required = false) String reason,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        try {
            orderService.cancelOrder(user, orderNumber, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Order " + orderNumber + " has been cancelled.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/account/orders/" + orderNumber;
    }
}
