package com.spmart.repository;

import com.spmart.entity.Category;
import com.spmart.entity.Product;
import com.spmart.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findBySlug(String slug);

    boolean existsBySku(String sku);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryAndActiveTrue(Category category, Pageable pageable);

    List<Product> findByFeaturedTrueAndActiveTrue();

    List<Product> findTop8ByActiveTrueOrderByCreatedAtDesc();

    List<Product> findTop8ByActiveTrueOrderByAverageRatingDesc();

    List<Product> findTop4ByCategoryAndActiveTrueAndIdNot(Category category, Long productId);

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.active = true ORDER BY p.brand ASC")
    List<String> findDistinctBrands();

    @Query("SELECT p FROM Product p WHERE p.active = true " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:brand IS NULL OR :brand = '' OR LOWER(p.brand) = LOWER(:brand)) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> filterCatalog(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchAdminProducts(@Param("keyword") String keyword, Pageable pageable);

    List<Product> findByStockLessThanEqualAndActiveTrue(Integer threshold);

    long countByStockLessThanEqualAndActiveTrue(Integer threshold);

    Page<Product> findBySeller(User seller, Pageable pageable);

    List<Product> findBySellerAndStockLessThanEqualAndActiveTrue(User seller, Integer threshold);

    long countBySeller(User seller);

    long countBySellerAndStockLessThanEqualAndActiveTrue(User seller, Integer threshold);

    Optional<Product> findByIdAndSeller(Long id, User seller);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(:maxBudget IS NULL OR p.price <= :maxBudget) AND " +
           "(:minBudget IS NULL OR p.price >= :minBudget) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.specifications) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchForAiAssistant(
            @Param("query") String query,
            @Param("minBudget") BigDecimal minBudget,
            @Param("maxBudget") BigDecimal maxBudget,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            Pageable pageable
    );
}
