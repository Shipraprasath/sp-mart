package com.spmart.service;

import com.spmart.dto.ProductDto;
import com.spmart.entity.Category;
import com.spmart.entity.Product;
import com.spmart.entity.User;
import com.spmart.exception.BusinessRuleException;
import com.spmart.exception.ResourceNotFoundException;
import com.spmart.repository.CategoryRepository;
import com.spmart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
    }

    @Transactional(readOnly = true)
    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Product> getNewArrivals() {
        return productRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Product> getBestSellers() {
        return productRepository.findTop8ByActiveTrueOrderByAverageRatingDesc();
    }

    @Transactional(readOnly = true)
    public List<Product> getRelatedProducts(Long productId) {
        Product current = getProductById(productId);
        if (current.getCategory() == null) {
            return Collections.emptyList();
        }
        return productRepository.findTop4ByCategoryAndActiveTrueAndIdNot(current.getCategory(), productId);
    }

    @Transactional(readOnly = true)
    public List<String> getAllBrands() {
        return productRepository.findDistinctBrands();
    }

    @Transactional(readOnly = true)
    public Page<Product> filterProducts(String keyword, Long categoryId, String brand,
                                       BigDecimal minPrice, BigDecimal maxPrice,
                                       String sortBy, int page, int size) {
        Sort sort = switch (sortBy != null ? sortBy.toLowerCase() : "newest") {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "rating" -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(Math.max(0, page), size > 0 ? size : 12, sort);
        return productRepository.filterCatalog(
                (keyword != null && !keyword.isBlank()) ? keyword.trim() : null,
                categoryId,
                (brand != null && !brand.isBlank()) ? brand.trim() : null,
                minPrice,
                maxPrice,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<Product> searchAdminProducts(String keyword, Pageable pageable) {
        return productRepository.searchAdminProducts(
                (keyword != null && !keyword.isBlank()) ? keyword.trim() : null,
                pageable
        );
    }

    public Product createProduct(ProductDto dto) {
        if (productRepository.existsBySku(dto.getSku().trim())) {
            throw new BusinessRuleException("A product with SKU '" + dto.getSku() + "' already exists.");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        String slug = generateSlug(dto.getName()) + "-" + dto.getSku().toLowerCase();

        Product product = new Product(
                dto.getSku().trim().toUpperCase(),
                dto.getName().trim(),
                slug,
                dto.getDescription(),
                dto.getBrand().trim(),
                category,
                dto.getPrice(),
                dto.getDiscountPrice(),
                dto.getStock(),
                dto.getMainImageUrl().trim(),
                dto.isFeatured()
        );
        product.setModelNumber(dto.getModelNumber());
        product.setSpecifications(dto.getSpecifications());
        product.setGalleryImages(dto.getGalleryImages());
        product.setActive(dto.isActive());

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductDto dto) {
        Product product = getProductById(id);

        if (!product.getSku().equalsIgnoreCase(dto.getSku().trim()) && productRepository.existsBySku(dto.getSku().trim())) {
            throw new BusinessRuleException("SKU '" + dto.getSku() + "' is already in use by another product.");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        product.setSku(dto.getSku().trim().toUpperCase());
        product.setName(dto.getName().trim());
        product.setDescription(dto.getDescription());
        product.setBrand(dto.getBrand().trim());
        product.setModelNumber(dto.getModelNumber());
        product.setSpecifications(dto.getSpecifications());
        product.setCategory(category);
        product.setPrice(dto.getPrice());
        product.setDiscountPrice(dto.getDiscountPrice());
        product.setStock(dto.getStock());
        product.setMainImageUrl(dto.getMainImageUrl().trim());
        product.setGalleryImages(dto.getGalleryImages());
        product.setActive(dto.isActive());
        product.setFeatured(dto.isFeatured());

        return productRepository.save(product);
    }

    public Product toggleProductActive(Long id) {
        Product product = getProductById(id);
        product.setActive(!product.isActive());
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsBySeller(User seller, Pageable pageable) {
        return productRepository.findBySeller(seller, pageable);
    }

    @Transactional(readOnly = true)
    public Product getSellerProductById(Long id, User seller) {
        Product product = productRepository.findByIdAndSeller(id, seller)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found or access denied."));
        return product;
    }

    public Product createSellerProduct(ProductDto dto, User seller) {
        if (productRepository.existsBySku(dto.getSku().trim())) {
            throw new BusinessRuleException("A product with SKU '" + dto.getSku() + "' already exists.");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        String slug = generateSlug(dto.getName()) + "-" + dto.getSku().toLowerCase();

        Product product = new Product(
                dto.getSku().trim().toUpperCase(),
                dto.getName().trim(),
                slug,
                dto.getDescription(),
                dto.getBrand().trim(),
                category,
                dto.getPrice(),
                dto.getDiscountPrice(),
                dto.getStock(),
                dto.getMainImageUrl().trim(),
                dto.isFeatured()
        );
        product.setModelNumber(dto.getModelNumber());
        product.setSpecifications(dto.getSpecifications());
        product.setGalleryImages(dto.getGalleryImages());
        product.setActive(dto.isActive());
        product.setSeller(seller);

        return productRepository.save(product);
    }

    public Product updateSellerProduct(Long id, ProductDto dto, User seller) {
        Product product = getSellerProductById(id, seller);

        if (!product.getSku().equalsIgnoreCase(dto.getSku().trim()) && productRepository.existsBySku(dto.getSku().trim())) {
            throw new BusinessRuleException("SKU '" + dto.getSku() + "' is already in use by another product.");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        product.setSku(dto.getSku().trim().toUpperCase());
        product.setName(dto.getName().trim());
        product.setDescription(dto.getDescription());
        product.setBrand(dto.getBrand().trim());
        product.setModelNumber(dto.getModelNumber());
        product.setSpecifications(dto.getSpecifications());
        product.setCategory(category);
        product.setPrice(dto.getPrice());
        product.setDiscountPrice(dto.getDiscountPrice());
        product.setStock(dto.getStock());
        product.setMainImageUrl(dto.getMainImageUrl().trim());
        product.setGalleryImages(dto.getGalleryImages());
        product.setActive(dto.isActive());
        product.setFeatured(dto.isFeatured());

        return productRepository.save(product);
    }

    public void deleteSellerProduct(Long id, User seller) {
        Product product = getSellerProductById(id, seller);
        productRepository.delete(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public long getSellerProductCount(User seller) {
        return productRepository.countBySeller(seller);
    }

    @Transactional(readOnly = true)
    public long getSellerLowStockCount(User seller, int threshold) {
        return productRepository.countBySellerAndStockLessThanEqualAndActiveTrue(seller, threshold);
    }

    @Transactional(readOnly = true)
    public List<Product> getSellerLowStockProducts(User seller, int threshold) {
        return productRepository.findBySellerAndStockLessThanEqualAndActiveTrue(seller, threshold);
    }

    @Transactional(readOnly = true)
    public List<Product> searchAiAssistant(String query, BigDecimal minBudget, BigDecimal maxBudget, Long categoryId, String brand, int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 4));
        return productRepository.searchForAiAssistant(
                (query != null && !query.isBlank()) ? query.trim() : null,
                minBudget,
                maxBudget,
                categoryId,
                (brand != null && !brand.isBlank()) ? brand.trim() : null,
                pageable
        );
    }

    public Product updateStock(Long productId, int newStock) {
        if (newStock < 0) {
            throw new BusinessRuleException("Stock quantity cannot be negative.");
        }
        Product product = getProductById(productId);
        product.setStock(newStock);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockLessThanEqualAndActiveTrue(threshold);
    }

    @Transactional(readOnly = true)
    public long getLowStockCount(int threshold) {
        return productRepository.countByStockLessThanEqualAndActiveTrue(threshold);
    }

    @Transactional(readOnly = true)
    public long getProductCount() {
        return productRepository.count();
    }

    private String generateSlug(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("^-|-$", "");
    }
}
