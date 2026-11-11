package com.spmart.controller;

import com.spmart.dto.ProductDto;
import com.spmart.entity.*;
import com.spmart.exception.BusinessRuleException;
import com.spmart.security.CustomUserDetails;
import com.spmart.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seller")
@PreAuthorize("hasRole('SELLER')")
public class SellerDashboardController {

    private final SellerService sellerService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;
    private final ReviewService reviewService;

    public SellerDashboardController(SellerService sellerService,
                                     ProductService productService,
                                     CategoryService categoryService,
                                     OrderService orderService,
                                     UserService userService,
                                     ReviewService reviewService) {
        this.sellerService = sellerService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    private User getSellerUser(CustomUserDetails userDetails) {
        return userService.findById(userDetails.getId());
    }

    private boolean checkApproved(User seller, Model model) {
        if (!sellerService.isApprovedSeller(seller)) {
            return false;
        }
        return true;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User seller = getSellerUser(userDetails);
        if (!checkApproved(seller, model)) {
            return "redirect:/seller/pending";
        }

        Map<String, Object> metrics = sellerService.getSellerDashboardMetrics(seller);
        List<Product> lowStockItems = productService.getSellerLowStockProducts(seller, 10);
        List<CustomerOrder> recentOrders = orderService.getOrdersBySeller(seller);
        if (recentOrders.size() > 5) {
            recentOrders = recentOrders.subList(0, 5);
        }

        model.addAttribute("metrics", metrics);
        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("seller", seller);
        model.addAttribute("profile", sellerService.getProfileByUser(seller));

        return "seller/dashboard";
    }

    @GetMapping("/products")
    public String products(@RequestParam(value = "page", defaultValue = "0") int page,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        User seller = getSellerUser(userDetails);
        if (!checkApproved(seller, model)) {
            return "redirect:/seller/pending";
        }

        Page<Product> productPage = productService.getProductsBySeller(seller, PageRequest.of(page, 10));
        model.addAttribute("productPage", productPage);
        model.addAttribute("profile", sellerService.getProfileByUser(seller));
        return "seller/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User seller = getSellerUser(userDetails);
        if (!checkApproved(seller, model)) {
            return "redirect:/seller/pending";
        }

        if (!model.containsAttribute("productDto")) {
            model.addAttribute("productDto", new ProductDto());
        }
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("isEdit", false);
        model.addAttribute("profile", sellerService.getProfileByUser(seller));
        return "seller/product-form";
    }

    @PostMapping("/products/new")
    public String createProduct(@Valid @ModelAttribute("productDto") ProductDto productDto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User seller = getSellerUser(userDetails);
        if (!checkApproved(seller, model)) {
            return "redirect:/seller/pending";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            model.addAttribute("isEdit", false);
            model.addAttribute("profile", sellerService.getProfileByUser(seller));
            return "seller/product-form";
        }

        try {
            productService.createSellerProduct(productDto, seller);
            redirectAttributes.addFlashAttribute("successMessage", "Product '" + productDto.getName() + "' created successfully!");
            return "redirect:/seller/products";
        } catch (BusinessRuleException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            model.addAttribute("isEdit", false);
            model.addAttribute("profile", sellerService.getProfileByUser(seller));
            return "seller/product-form";
        }
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        User seller = getSellerUser(userDetails);
        if (!checkApproved(seller, model)) {
            return "redirect:/seller/pending";
        }

        Product product = productService.getSellerProductById(id, seller);
        ProductDto dto = ProductDto.fromProduct(product);

        model.addAttribute("productDto", dto);
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("isEdit", true);
        model.addAttribute("productId", id);
        model.addAttribute("profile", sellerService.getProfileByUser(seller));
        return "seller/product-form";
    }

    @PostMapping("/products/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productDto") ProductDto productDto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User seller = getSellerUser(userDetails);
        if (!checkApproved(seller, model)) {
            return "redirect:/seller/pending";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            model.addAttribute("isEdit", true);
            model.addAttribute("productId", id);
            model.addAttribute("profile", sellerService.getProfileByUser(seller));
            return "seller/product-form";
        }

        try {
            productService.updateSellerProduct(id, productDto, seller);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            return "redirect:/seller/products";
        } catch (BusinessRuleException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            model.addAttribute("isEdit", true);
            model.addAttribute("productId", id);
            model.addAttribute("profile", sellerService.getProfileByUser(seller));
            return "seller/product-form";
        }
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User seller = getSellerUser(userDetails);
        try {
            productService.deleteSellerProduct(id, seller);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/seller/products";
    }

    @GetMapping("/orders")
    public String orders(@RequestParam(value = "page", defaultValue = "0") int page,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model) {
        User seller = getSellerUser(userDetails);
        if (!checkApproved(seller, model)) {
            return "redirect:/seller/pending";
        }

        Page<CustomerOrder> orderPage = orderService.getOrdersBySeller(seller, PageRequest.of(page, 10));
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("profile", sellerService.getProfileByUser(seller));
        return "seller/orders";
    }

    @GetMapping("/orders/{orderNumber}")
    public String orderDetails(@PathVariable String orderNumber,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        User seller = getSellerUser(userDetails);
        if (!checkApproved(seller, model)) {
            return "redirect:/seller/pending";
        }

        CustomerOrder order = orderService.getOrderByNumber(orderNumber);
        model.addAttribute("order", order);
        model.addAttribute("seller", seller);
        model.addAttribute("profile", sellerService.getProfileByUser(seller));
        return "seller/order-details";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam("status") OrderStatus newStatus,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        User seller = getSellerUser(userDetails);
        try {
            orderService.updateSellerOrderStatus(id, newStatus, seller);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + newStatus.getDisplayName() + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/seller/orders";
    }

    @GetMapping("/reports")
    public String reports(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User seller = getSellerUser(userDetails);
        if (!checkApproved(seller, model)) {
            return "redirect:/seller/pending";
        }

        Map<String, Object> metrics = sellerService.getSellerDashboardMetrics(seller);
        List<Review> reviews = reviewService.getSellerReviews(seller);

        model.addAttribute("metrics", metrics);
        model.addAttribute("reviews", reviews);
        model.addAttribute("profile", sellerService.getProfileByUser(seller));
        return "seller/reports";
    }
}
