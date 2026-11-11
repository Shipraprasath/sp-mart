package com.spmart.service;

import com.spmart.dto.AddressDto;
import com.spmart.dto.CheckoutDto;
import com.spmart.entity.*;
import com.spmart.exception.BusinessRuleException;
import com.spmart.repository.CategoryRepository;
import com.spmart.repository.CustomerOrderRepository;
import com.spmart.repository.ProductRepository;
import com.spmart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerOrderRepository orderRepository;

    private User customer;
    private Product product;
    private Address address;

    @BeforeEach
    void setUp() {
        customer = userRepository.save(new User("Charlie Brown", "charlie@example.com", "pass", "9876543210", Role.ROLE_CUSTOMER));

        Category category = categoryRepository.save(new Category("Audio", "audio", "Headphones", "img", "icon", 1));

        product = productRepository.save(new Product(
                "ORD-TEST-PROD", "Noise Canceling Buds", "buds", "desc",
                "Sony", category, new BigDecimal("200.00"), null, 10, "img.jpg", true
        ));

        AddressDto addrDto = new AddressDto();
        addrDto.setRecipientName("Charlie");
        addrDto.setPhone("9876543210");
        addrDto.setAddressLine1("Street 1");
        addrDto.setLocality("Downtown");
        addrDto.setCity("Bengaluru");
        addrDto.setState("Karnataka");
        addrDto.setPostalCode("560001");
        address = addressService.createAddress(customer, addrDto);
    }

    @Test
    @DisplayName("13. Order placement decrements inventory and creates immutable item snapshots")
    void testCreateOrder_InventoryDecrement() {
        cartService.addToCart(customer, product.getId(), 3);

        CheckoutDto checkoutDto = new CheckoutDto();
        checkoutDto.setAddressId(address.getId());
        checkoutDto.setPaymentMethod(PaymentMethod.COD);
        checkoutDto.setIdempotencyKey(UUID.randomUUID().toString());

        CustomerOrder order = orderService.createOrder(customer, checkoutDto);

        assertNotNull(order);
        assertNotNull(order.getOrderNumber());
        assertEquals(OrderStatus.PLACED, order.getOrderStatus());
        assertEquals(1, order.getItems().size());
        assertEquals(3, order.getItems().get(0).getQuantity());

        // Verify product inventory was decremented from 10 to 7
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(7, updatedProduct.getStock());

        // Verify shopping cart was cleared
        Cart cart = cartService.getOrCreateCart(customer);
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("14. Idempotency protection prevents duplicate orders upon repeat submission")
    void testCreateOrder_Idempotency() {
        cartService.addToCart(customer, product.getId(), 2);
        String idempotencyKey = "UNIQUE-IDEMP-KEY-999";

        CheckoutDto checkoutDto1 = new CheckoutDto();
        checkoutDto1.setAddressId(address.getId());
        checkoutDto1.setPaymentMethod(PaymentMethod.UPI);
        checkoutDto1.setIdempotencyKey(idempotencyKey);

        CustomerOrder order1 = orderService.createOrder(customer, checkoutDto1);

        // Second click with identical idempotency key returns the already-created order
        CheckoutDto checkoutDto2 = new CheckoutDto();
        checkoutDto2.setAddressId(address.getId());
        checkoutDto2.setPaymentMethod(PaymentMethod.UPI);
        checkoutDto2.setIdempotencyKey(idempotencyKey);

        CustomerOrder order2 = orderService.createOrder(customer, checkoutDto2);

        assertEquals(order1.getId(), order2.getId());
        assertEquals(order1.getOrderNumber(), order2.getOrderNumber());
    }

    @Test
    @DisplayName("15. Order cancellation restores product stock safely")
    void testCancelOrder_RestoresInventory() {
        cartService.addToCart(customer, product.getId(), 4);

        CheckoutDto checkoutDto = new CheckoutDto();
        checkoutDto.setAddressId(address.getId());
        checkoutDto.setPaymentMethod(PaymentMethod.COD);
        CustomerOrder order = orderService.createOrder(customer, checkoutDto);

        // Inventory should now be 10 - 4 = 6
        assertEquals(6, productRepository.findById(product.getId()).orElseThrow().getStock());

        // Cancel order
        CustomerOrder cancelled = orderService.cancelOrder(customer, order.getOrderNumber(), "Ordered wrong item");
        assertEquals(OrderStatus.CANCELLED, cancelled.getOrderStatus());

        // Inventory must be fully restored back to 10
        assertEquals(10, productRepository.findById(product.getId()).orElseThrow().getStock());
    }

    @Test
    @DisplayName("16. Reject invalid order status transitions on the server")
    void testOrderStatusTransitionValidation() {
        cartService.addToCart(customer, product.getId(), 1);
        CheckoutDto checkoutDto = new CheckoutDto();
        checkoutDto.setAddressId(address.getId());
        checkoutDto.setPaymentMethod(PaymentMethod.COD);
        CustomerOrder order = orderService.createOrder(customer, checkoutDto);

        // Valid: PLACED -> CONFIRMED
        orderService.adminUpdateOrderStatus(order.getId(), OrderStatus.CONFIRMED, "OK", "Admin");

        // Invalid: CONFIRMED cannot jump directly to DELIVERED without SHIPPED/OUT_FOR_DELIVERY
        assertThrows(BusinessRuleException.class, () ->
                orderService.adminUpdateOrderStatus(order.getId(), OrderStatus.DELIVERED, "Skip", "Admin")
        );
    }
}
