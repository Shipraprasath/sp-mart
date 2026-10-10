package com.spmart.controller;

import com.spmart.dto.CheckoutDto;
import com.spmart.entity.*;
import com.spmart.exception.BusinessRuleException;
import com.spmart.exception.InsufficientStockException;
import com.spmart.security.CustomUserDetails;
import com.spmart.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
public class CheckoutController {

    private final OrderService orderService;
    private final CartService cartService;
    private final AddressService addressService;
    private final UserService userService;
    private final PaymentService paymentService;

    public CheckoutController(OrderService orderService,
                              CartService cartService,
                              AddressService addressService,
                              UserService userService,
                              PaymentService paymentService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.addressService = addressService;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @GetMapping("/checkout")
    public String checkoutPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId());
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.isEmpty()) {
            return "redirect:/cart";
        }

        List<Address> addresses = addressService.getUserAddresses(user);
        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal shippingFee = cartService.calculateShippingFee(subtotal);
        BigDecimal finalTotal = subtotal.add(shippingFee);

        CheckoutDto checkoutDto = new CheckoutDto();
        Address defaultAddr = addressService.getDefaultAddress(user);
        if (defaultAddr != null) {
            checkoutDto.setAddressId(defaultAddr.getId());
        }
        checkoutDto.setPaymentMethod(PaymentMethod.COD);
        checkoutDto.setIdempotencyKey(UUID.randomUUID().toString());

        model.addAttribute("cart", cart);
        model.addAttribute("addresses", addresses);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("finalTotal", finalTotal);
        model.addAttribute("checkoutDto", checkoutDto);

        return "checkout/checkout";
    }

    @PostMapping("/checkout/place-order")
    public String placeOrder(@Valid @ModelAttribute("checkoutDto") CheckoutDto checkoutDto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());

        if (bindingResult.hasErrors()) {
            Cart cart = cartService.getOrCreateCart(user);
            List<Address> addresses = addressService.getUserAddresses(user);
            BigDecimal subtotal = cart.getSubtotal();
            BigDecimal shippingFee = cartService.calculateShippingFee(subtotal);
            BigDecimal finalTotal = subtotal.add(shippingFee);

            model.addAttribute("cart", cart);
            model.addAttribute("addresses", addresses);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("finalTotal", finalTotal);
            return "checkout/checkout";
        }

        try {
            CustomerOrder order = orderService.createOrder(user, checkoutDto);

            if (order.getPaymentMethod() == PaymentMethod.COD) {
                redirectAttributes.addFlashAttribute("successMessage", "Order placed successfully!");
                return "redirect:/orders/" + order.getOrderNumber() + "/confirmation";
            } else {
                return "redirect:/checkout/payment/" + order.getOrderNumber();
            }
        } catch (InsufficientStockException | BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/checkout/payment/{orderNumber}")
    public String paymentPage(@PathVariable String orderNumber,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        User user = userService.findById(userDetails.getId());
        CustomerOrder order = orderService.getOrderByNumberAndUser(orderNumber, user);
        Payment payment = paymentService.getPaymentByOrder(order);
        String upiUri = paymentService.generateUpiUri(order);

        model.addAttribute("order", order);
        model.addAttribute("payment", payment);
        model.addAttribute("upiUri", upiUri);
        model.addAttribute("merchantUpiId", paymentService.getMerchantUpiId());

        return "checkout/payment";
    }

    @PostMapping("/checkout/payment/{orderNumber}/process")
    public String processSimulatedPayment(@PathVariable String orderNumber,
                                         @RequestParam(value = "simulatedTxnId", required = false) String txnId,
                                         @RequestParam(value = "status", defaultValue = "SUCCESS") String status,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        CustomerOrder order = orderService.getOrderByNumberAndUser(orderNumber, user);

        boolean isSuccess = "SUCCESS".equalsIgnoreCase(status);
        paymentService.completeDemoPayment(order, txnId, isSuccess);

        if (isSuccess) {
            redirectAttributes.addFlashAttribute("successMessage", "Payment confirmed successfully!");
            return "redirect:/orders/" + order.getOrderNumber() + "/confirmation";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment simulation failed or was cancelled.");
            return "redirect:/checkout/payment/" + orderNumber;
        }
    }

    @GetMapping("/orders/{orderNumber}/confirmation")
    public String orderConfirmation(@PathVariable String orderNumber,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model) {
        User user = userService.findById(userDetails.getId());
        CustomerOrder order = orderService.getOrderByNumberAndUser(orderNumber, user);
        Payment payment = paymentService.getPaymentByOrder(order);

        model.addAttribute("order", order);
        model.addAttribute("payment", payment);

        return "checkout/confirmation";
    }
}
