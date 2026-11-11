package com.spmart.controller;

import com.spmart.dto.ProductDto;
import com.spmart.entity.Product;
import com.spmart.exception.BusinessRuleException;
import com.spmart.service.CategoryService;
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

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listProducts(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               Model model) {
        Page<Product> productPage = productService.searchAdminProducts(
                keyword,
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", keyword);
        return "admin/products";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        if (!model.containsAttribute("productDto")) {
            model.addAttribute("productDto", new ProductDto());
        }
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        return "admin/product-form";
    }

    @PostMapping("/new")
    public String createProduct(@Valid @ModelAttribute("productDto") ProductDto productDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            return "admin/product-form";
        }

        try {
            productService.createProduct(productDto);
            redirectAttributes.addFlashAttribute("successMessage", "Product created successfully.");
            return "redirect:/admin/products";
        } catch (BusinessRuleException ex) {
            bindingResult.rejectValue("sku", "error.productDto", ex.getMessage());
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            return "admin/product-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBrand(product.getBrand());
        dto.setCategoryId(product.getCategory().getId());
        dto.setPrice(product.getPrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setStock(product.getStock());
        dto.setMainImageUrl(product.getMainImageUrl());
        dto.setGalleryImages(product.getGalleryImages());
        dto.setActive(product.isActive());
        dto.setFeatured(product.isFeatured());

        model.addAttribute("productDto", dto);
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        return "admin/product-form";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productDto") ProductDto productDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            return "admin/product-form";
        }

        try {
            productService.updateProduct(id, productDto);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully.");
            return "redirect:/admin/products";
        } catch (BusinessRuleException ex) {
            bindingResult.rejectValue("sku", "error.productDto", ex.getMessage());
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            return "admin/product-form";
        }
    }

    @PostMapping("/toggle/{id}")
    public String toggleProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Product p = productService.toggleProductActive(id);
        redirectAttributes.addFlashAttribute("successMessage", 
                "Product status updated to " + (p.isActive() ? "Active" : "Inactive") + ".");
        return "redirect:/admin/products";
    }
}
