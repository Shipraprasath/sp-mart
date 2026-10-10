package com.spmart.controller;

import com.spmart.dto.ReviewDto;
import com.spmart.entity.Category;
import com.spmart.entity.Product;
import com.spmart.entity.Review;
import com.spmart.entity.User;
import com.spmart.security.CustomUserDetails;
import com.spmart.service.*;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductCatalogController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;
    private final UserService userService;

    public ProductCatalogController(ProductService productService,
                                    CategoryService categoryService,
                                    ReviewService reviewService,
                                    WishlistService wishlistService,
                                    UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.wishlistService = wishlistService;
        this.userService = userService;
    }

    @GetMapping
    public String catalog(@RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(value = "category", required = false) Long categoryId,
                          @RequestParam(value = "brand", required = false) String brand,
                          @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                          @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                          @RequestParam(value = "sortBy", required = false, defaultValue = "newest") String sortBy,
                          @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                          Model model) {

        Page<Product> productPage = productService.filterProducts(
                keyword, categoryId, brand, minPrice, maxPrice, sortBy, page, 12
        );

        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("brands", productService.getAllBrands());
        model.addAttribute("allCategories", categoryService.getAllActiveCategories());

        return "products/catalog";
    }

    @GetMapping("/category/{slug}")
    public String categoryCatalog(@PathVariable String slug,
                                  @RequestParam(value = "sortBy", required = false, defaultValue = "newest") String sortBy,
                                  @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                  Model model) {
        Category category = categoryService.getCategoryBySlug(slug);
        return catalog(null, category.getId(), null, null, null, sortBy, page, model);
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String query,
                         @RequestParam(value = "sortBy", required = false, defaultValue = "newest") String sortBy,
                         @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                         Model model) {
        return catalog(query, null, null, null, null, sortBy, page, model);
    }

    @GetMapping("/{id}")
    public String productDetails(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        Product product = productService.getProductById(id);
        List<Product> relatedProducts = productService.getRelatedProducts(id);
        List<Review> reviews = reviewService.getProductReviews(id);

        boolean inWishlist = false;
        boolean canReview = false;

        if (userDetails != null) {
            User user = userService.findById(userDetails.getId());
            inWishlist = wishlistService.isProductInWishlist(user, id);
            canReview = reviewService.canUserReviewProduct(user, id);
        }

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("reviews", reviews);
        model.addAttribute("inWishlist", inWishlist);
        model.addAttribute("canReview", canReview);
        model.addAttribute("reviewDto", new ReviewDto(id));

        return "products/details";
    }
}
