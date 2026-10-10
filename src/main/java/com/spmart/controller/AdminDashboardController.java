package com.spmart.controller;

import com.spmart.entity.CustomerOrder;
import com.spmart.entity.Product;
import com.spmart.service.CategoryService;
import com.spmart.service.OrderService;
import com.spmart.service.ProductService;
import com.spmart.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final OrderService orderService;

    public AdminDashboardController(ProductService productService,
                                    CategoryService categoryService,
                                    UserService userService,
                                    OrderService orderService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        long totalProducts = productService.getProductCount();
        long totalCategories = categoryService.getCategoryCount();
        long totalCustomers = userService.getCustomerCount();
        long totalOrders = orderService.getOrderCount();
        long pendingOrders = orderService.getPendingOrderCount();
        long deliveredOrders = orderService.getDeliveredOrderCount();
        long lowStockCount = productService.getLowStockCount(10);
        BigDecimal totalRevenue = orderService.calculateRevenue();

        List<Product> lowStockItems = productService.getLowStockProducts(10);
        List<CustomerOrder> recentOrders = orderService.getRecentOrders();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("recentOrders", recentOrders);

        return "admin/dashboard";
    }
}
