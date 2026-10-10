package com.spmart.controller;

import com.spmart.dto.InventoryUpdateDto;
import com.spmart.entity.Product;
import com.spmart.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/inventory")
public class AdminInventoryController {

    private final ProductService productService;

    public AdminInventoryController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String inventoryView(@RequestParam(value = "lowStockOnly", required = false, defaultValue = "false") boolean lowStockOnly,
                                @RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                Model model) {
        if (lowStockOnly) {
            List<Product> lowStockProducts = productService.getLowStockProducts(10);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("isLowStockFilter", true);
        } else {
            Page<Product> productPage = productService.searchAdminProducts(
                    keyword,
                    PageRequest.of(page, 15, Sort.by(Sort.Direction.ASC, "stock"))
            );
            model.addAttribute("productPage", productPage);
            model.addAttribute("isLowStockFilter", false);
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("lowStockCount", productService.getLowStockCount(10));
        return "admin/inventory";
    }

    @PostMapping("/update")
    public String updateStock(@Valid @ModelAttribute InventoryUpdateDto dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid stock quantity specified.");
            return "redirect:/admin/inventory";
        }
        productService.updateStock(dto.getProductId(), dto.getNewStock());
        redirectAttributes.addFlashAttribute("successMessage", "Inventory stock updated successfully.");
        return "redirect:/admin/inventory";
    }
}
