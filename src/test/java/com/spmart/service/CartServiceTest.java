package com.spmart.service;

import com.spmart.entity.*;
import com.spmart.exception.InsufficientStockException;
import com.spmart.repository.CategoryRepository;
import com.spmart.repository.ProductRepository;
import com.spmart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private Product inStockProduct;
    private Product lowStockProduct;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User("Test Customer", "carttest@example.com", "secret", "9876543210", Role.ROLE_CUSTOMER));

        Category category = categoryRepository.save(new Category("Gadgets", "gadgets", "Desc", "img", "icon", 1));

        inStockProduct = productRepository.save(new Product(
                "TEST-SKU-1", "Wireless Keyboard", "wireless-keyboard", "Great keyboard",
                "Logitech", category, new BigDecimal("100.00"), new BigDecimal("80.00"), 20, "img.jpg", true
        ));

        lowStockProduct = productRepository.save(new Product(
                "TEST-SKU-2", "Limited Edition Mug", "limited-mug", "Ceramic mug",
                "BrandX", category, new BigDecimal("25.00"), null, 2, "mug.jpg", false
        ));
    }

    @Test
    @DisplayName("6. Add product to cart and verify line totals and subtotal")
    void testAddToCartAndCalculations() {
        Cart cart = cartService.addToCart(testUser, inStockProduct.getId(), 2);

        assertNotNull(cart);
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getTotalQuantity());

        // Price is discount price: 80.00 * 2 = 160.00
        assertEquals(0, new BigDecimal("160.00").compareTo(cart.getSubtotal()));
    }

    @Test
    @DisplayName("7. Stock validation prevents purchasing more units than available in stock")
    void testAddToCart_InsufficientStock() {
        assertThrows(InsufficientStockException.class, () ->
                cartService.addToCart(testUser, lowStockProduct.getId(), 5)
        );
    }

    @Test
    @DisplayName("8. Incremental quantity update checks cumulative stock")
    void testUpdateCartQuantity() {
        Cart cart = cartService.addToCart(testUser, inStockProduct.getId(), 1);
        CartItem item = cart.getItems().get(0);

        Cart updated = cartService.updateQuantity(testUser, item.getId(), 3);
        assertEquals(3, updated.getTotalQuantity());
        assertEquals(0, new BigDecimal("240.00").compareTo(updated.getSubtotal()));

        // Cannot exceed available stock
        assertThrows(InsufficientStockException.class, () ->
                cartService.updateQuantity(testUser, item.getId(), 50)
        );
    }

    @Test
    @DisplayName("9. Shipping fee calculation respects free-shipping threshold")
    void testCalculateShippingFee() {
        // Below 500 threshold
        BigDecimal feeBelow = cartService.calculateShippingFee(new BigDecimal("250.00"));
        assertEquals(0, new BigDecimal("50.00").compareTo(feeBelow));

        // Equal or above 500 threshold
        BigDecimal feeAbove = cartService.calculateShippingFee(new BigDecimal("500.00"));
        assertEquals(BigDecimal.ZERO, feeAbove);

        BigDecimal feeSuper = cartService.calculateShippingFee(new BigDecimal("1200.00"));
        assertEquals(BigDecimal.ZERO, feeSuper);
    }

    @Test
    @DisplayName("10. Remove item and clear cart")
    void testRemoveAndClearCart() {
        Cart cart = cartService.addToCart(testUser, inStockProduct.getId(), 1);
        cartService.addToCart(testUser, lowStockProduct.getId(), 1);
        assertEquals(2, cart.getItems().size());

        cartService.clearCart(testUser);
        Cart cleared = cartService.getOrCreateCart(testUser);
        assertTrue(cleared.isEmpty());
    }
}
