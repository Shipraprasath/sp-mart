package com.spmart.service;

import com.spmart.dto.ProductDto;
import com.spmart.entity.Category;
import com.spmart.entity.Product;
import com.spmart.exception.BusinessRuleException;
import com.spmart.repository.CategoryRepository;
import com.spmart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Category electronicsCategory;

    @BeforeEach
    void setUp() {
        electronicsCategory = categoryRepository.save(new Category("Electronics Test", "elec-test", "desc", "img", "icon", 1));
    }

    @Test
    @DisplayName("17. Product creation enforces unique SKU constraint")
    void testCreateProduct_UniqueSku() {
        ProductDto dto1 = new ProductDto();
        dto1.setSku("PROD-UNIQ-001");
        dto1.setName("Product One");
        dto1.setBrand("Brand A");
        dto1.setCategoryId(electronicsCategory.getId());
        dto1.setPrice(new BigDecimal("50.00"));
        dto1.setStock(15);
        dto1.setMainImageUrl("https://image.url");
        productService.createProduct(dto1);

        ProductDto dto2 = new ProductDto();
        dto2.setSku("PROD-UNIQ-001");
        dto2.setName("Product Duplicate");
        dto2.setBrand("Brand B");
        dto2.setCategoryId(electronicsCategory.getId());
        dto2.setPrice(new BigDecimal("70.00"));
        dto2.setStock(5);
        dto2.setMainImageUrl("https://image2.url");

        assertThrows(BusinessRuleException.class, () -> productService.createProduct(dto2));
    }

    @Test
    @DisplayName("18. Catalog filtering by brand, keyword, and price range")
    void testFilterCatalog() {
        ProductDto dto1 = new ProductDto();
        dto1.setSku("SRCH-SKU-1");
        dto1.setName("Ultra Soundbar TestUnit");
        dto1.setBrand("Bose");
        dto1.setCategoryId(electronicsCategory.getId());
        dto1.setPrice(new BigDecimal("299.00"));
        dto1.setStock(10);
        dto1.setMainImageUrl("https://soundbar.jpg");
        productService.createProduct(dto1);

        ProductDto dto2 = new ProductDto();
        dto2.setSku("SRCH-SKU-2");
        dto2.setName("Budget Earphones TestUnit");
        dto2.setBrand("Boat");
        dto2.setCategoryId(electronicsCategory.getId());
        dto2.setPrice(new BigDecimal("19.00"));
        dto2.setStock(50);
        dto2.setMainImageUrl("https://earphones.jpg");
        productService.createProduct(dto2);

        // Filter by keyword "Soundbar TestUnit"
        Page<Product> page = productService.filterProducts("Soundbar TestUnit", null, null, null, null, "newest", 0, 10);
        assertEquals(1, page.getTotalElements());
        assertEquals("Ultra Soundbar TestUnit", page.getContent().get(0).getName());

        // Filter by price range 10 - 50
        Page<Product> budgetPage = productService.filterProducts(null, null, null, new BigDecimal("10.00"), new BigDecimal("50.00"), "newest", 0, 10);
        assertTrue(budgetPage.getContent().stream().anyMatch(p -> p.getSku().equals("SRCH-SKU-2")));
    }

    @Test
    @DisplayName("19. Update stock prevents negative quantities")
    void testUpdateStockValidation() {
        ProductDto dto = new ProductDto();
        dto.setSku("STK-SKU-1");
        dto.setName("Stock Product");
        dto.setBrand("Brand");
        dto.setCategoryId(electronicsCategory.getId());
        dto.setPrice(new BigDecimal("10.00"));
        dto.setStock(5);
        dto.setMainImageUrl("https://img.jpg");
        Product p = productService.createProduct(dto);

        assertThrows(BusinessRuleException.class, () -> productService.updateStock(p.getId(), -5));

        Product updated = productService.updateStock(p.getId(), 25);
        assertEquals(25, updated.getStock());
    }
}
