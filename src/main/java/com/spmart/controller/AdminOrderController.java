package com.spmart.controller;

import com.spmart.entity.CustomerOrder;
import com.spmart.entity.OrderStatus;
import com.spmart.entity.OrderStatusHistory;
import com.spmart.entity.Payment;
import com.spmart.exception.BusinessRuleException;
import com.spmart.security.CustomUserDetails;
import com.spmart.service.OrderService;
import com.spmart.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    public AdminOrderController(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public String listOrders(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "status", required = false) OrderStatus status,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             Model model) {
        Page<CustomerOrder> orderPage = orderService.adminSearchOrders(
                keyword, status, PageRequest.of(page, 15)
        );

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("allStatuses", OrderStatus.values());
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        CustomerOrder order = orderService.getOrderById(id);
        Payment payment = paymentService.getPaymentByOrder(order);
        List<OrderStatusHistory> history = orderService.getOrderHistory(order);

        model.addAttribute("order", order);
        model.addAttribute("payment", payment);
        model.addAttribute("statusHistory", history);
        model.addAttribute("allStatuses", OrderStatus.values());
        return "admin/order-details";
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam("status") OrderStatus newStatus,
                                    @RequestParam(value = "comment", required = false) String comment,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.adminUpdateOrderStatus(id, newStatus, comment, userDetails.getFullName());
            redirectAttributes.addFlashAttribute("successMessage", "Order status successfully updated to " + newStatus.getDisplayName() + ".");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}
