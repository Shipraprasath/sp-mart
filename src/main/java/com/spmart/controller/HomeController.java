package com.spmart.controller;

import com.spmart.entity.Category;
import com.spmart.entity.Product;
import com.spmart.entity.Review;
import com.spmart.service.CategoryService;
import com.spmart.service.ProductService;
import com.spmart.service.ReviewService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;

    public HomeController(ProductService productService,
                          CategoryService categoryService,
                          ReviewService reviewService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Product> featured = productService.getFeaturedProducts();
        List<Product> newArrivals = productService.getNewArrivals();
        List<Product> bestSellers = productService.getBestSellers();
        List<Category> categories = categoryService.getAllActiveCategories();

        // Today's best deals: products with highest discounts
        List<Product> bestDeals = featured.stream()
                .filter(Product::hasDiscount)
                .sorted((a, b) -> Integer.compare(b.getDiscountPercentage(), a.getDiscountPercentage()))
                .limit(4)
                .collect(Collectors.toList());
        if (bestDeals.isEmpty()) {
            bestDeals = newArrivals.stream().limit(4).collect(Collectors.toList());
        }

        // Smart-Home essentials
        Optional<Category> smartHomeCat = categoryService.getAllActiveCategories().stream()
                .filter(c -> c.getSlug().contains("smart-home"))
                .findFirst();
        List<Product> smartHomeProducts = smartHomeCat
                .map(c -> productService.filterProducts(null, c.getId(), null, null, null, "rating", 0, 4).getContent())
                .orElse(List.of());

        // Recommended products ("Picked for Your Next Upgrade")
        List<Product> recommendations = bestSellers.stream()
                .skip(Math.min(2, bestSellers.size()))
                .limit(4)
                .collect(Collectors.toList());
        if (recommendations.isEmpty()) {
            recommendations = featured.stream().limit(4).collect(Collectors.toList());
        }

        // Recent customer reviews
        List<Review> customerReviews = reviewService.getAllReviews(PageRequest.of(0, 4)).getContent();

        model.addAttribute("featuredProducts", featured);
        model.addAttribute("newArrivals", newArrivals);
        model.addAttribute("bestSellers", bestSellers);
        model.addAttribute("bestDeals", bestDeals);
        model.addAttribute("smartHomeProducts", smartHomeProducts);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("categories", categories);
        model.addAttribute("customerReviews", customerReviews);

        return "index";
    }
}
