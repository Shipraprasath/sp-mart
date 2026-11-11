package com.spmart.service;

import com.spmart.dto.CheckoutDto;
import com.spmart.entity.*;
import com.spmart.exception.BusinessRuleException;
import com.spmart.exception.InsufficientStockException;
import com.spmart.exception.ResourceNotFoundException;
import com.spmart.exception.UnauthorizedAccessException;
import com.spmart.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;
    private final AddressService addressService;
    private final ProductRepository productRepository;

    public OrderService(CustomerOrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OrderStatusHistoryRepository historyRepository,
                        PaymentRepository paymentRepository,
                        CartService cartService,
                        AddressService addressService,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.historyRepository = historyRepository;
        this.paymentRepository = paymentRepository;
        this.cartService = cartService;
        this.addressService = addressService;
        this.productRepository = productRepository;
    }

    public CustomerOrder createOrder(User user, CheckoutDto dto) {
        // Idempotency check to prevent duplicate orders on double submissions
        if (dto.getIdempotencyKey() != null && !dto.getIdempotencyKey().isBlank()) {
            if (orderRepository.existsByIdempotencyKey(dto.getIdempotencyKey().trim())) {
                return orderRepository.findAll().stream()
                        .filter(o -> dto.getIdempotencyKey().trim().equals(o.getIdempotencyKey()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessRuleException("Duplicate order submission detected."));
            }
        }

        Cart cart = cartService.getOrCreateCart(user);
        if (cart.isEmpty()) {
            throw new BusinessRuleException("Your shopping cart is empty. Please add items before checking out.");
        }

        Address address = addressService.getAddressByIdAndUser(dto.getAddressId(), user);

        // Revalidate stock & recalculate amounts on backend
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProduct().getName()));

            if (!product.isActive()) {
                throw new BusinessRuleException("Product '" + product.getName() + "' is no longer available.");
            }

            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product '" + product.getName() 
                        + "'. Available: " + product.getStock() + ", in cart: " + item.getQuantity());
            }

            BigDecimal lineTotal = product.getEffectivePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal shippingFee = cartService.calculateShippingFee(subtotal);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = subtotal.add(shippingFee).subtract(discountAmount);

        // Generate unique order number: SPM-YYYYMMDD-XXXX
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String orderNumber = "SPM-" + datePart + "-" + randomPart;

        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber(orderNumber);
        order.setUser(user);
        order.setRecipientName(address.getRecipientName());
        order.setRecipientPhone(address.getPhone());
        order.setDeliveryAddressText(address.getFormattedAddress());
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setShippingFee(shippingFee);
        order.setFinalAmount(finalAmount);
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderStatus(OrderStatus.PLACED);
        order.setIdempotencyKey(dto.getIdempotencyKey());
        order.setNotes(dto.getNotes());

        CustomerOrder savedOrder = orderRepository.save(order);

        // Create immutable snapshots & decrement product inventory
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            // Decrement inventory transactionally
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(
                    savedOrder,
                    product,
                    product.getSeller(),
                    product.getName(),
                    product.getSku(),
                    product.getMainImageUrl(),
                    product.getEffectivePrice(),
                    item.getQuantity(),
                    product.getEffectivePrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
            savedOrder.getItems().add(orderItem);
            orderItemRepository.save(orderItem);
        }

        // Record initial status audit trail
        OrderStatusHistory history = new OrderStatusHistory(
                savedOrder,
                OrderStatus.PLACED,
                "Order created with payment method " + dto.getPaymentMethod().getDisplayName(),
                user.getFullName()
        );
        historyRepository.save(history);

        // Initialize payment record
        String txnRef = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Payment payment = new Payment(
                savedOrder,
                txnRef,
                dto.getPaymentMethod(),
                finalAmount,
                PaymentStatus.PENDING,
                "Payment initialized (" + dto.getPaymentMethod().name() + ")"
        );
        paymentRepository.save(payment);

        // Clear cart now that order is securely saved
        cartService.clearCart(user);

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public CustomerOrder getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
    }

    @Transactional(readOnly = true)
    public CustomerOrder getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public CustomerOrder getOrderByNumberAndUser(String orderNumber, User user) {
        CustomerOrder order = getOrderByNumber(orderNumber);
        if (!order.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new UnauthorizedAccessException("You are not authorized to view this order.");
        }
        return order;
    }

    @Transactional(readOnly = true)
    public Page<CustomerOrder> getUserOrders(User user, Pageable pageable) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public CustomerOrder cancelOrder(User user, String orderNumber, String reason) {
        CustomerOrder order = getOrderByNumberAndUser(orderNumber, user);

        if (!order.isCancellable()) {
            throw new BusinessRuleException("This order cannot be cancelled in its current state (" 
                    + order.getOrderStatus().getDisplayName() + ").");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        // Restore inventory for each item
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        // Update payment status if already paid
        Payment payment = paymentRepository.findByOrder(order).orElse(null);
        if (payment != null && payment.getPaymentStatus() == PaymentStatus.PAID) {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            payment.setGatewayResponse("Refund initiated due to order cancellation.");
            paymentRepository.save(payment);
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        // Record history
        OrderStatusHistory history = new OrderStatusHistory(
                order,
                OrderStatus.CANCELLED,
                "Cancelled by " + user.getFullName() + ". Reason: " + (reason != null ? reason : "Customer request"),
                user.getFullName()
        );
        historyRepository.save(history);

        return orderRepository.save(order);
    }

    public CustomerOrder adminUpdateOrderStatus(Long orderId, OrderStatus newStatus, String comment, String adminName) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus currentStatus = order.getOrderStatus();
        if (currentStatus == newStatus) {
            return order;
        }

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BusinessRuleException("Invalid status transition from " + currentStatus.getDisplayName() 
                    + " to " + newStatus.getDisplayName() + ".");
        }

        order.setOrderStatus(newStatus);

        // If transitioning to CANCELLED, restore inventory
        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    Product product = item.getProduct();
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                }
            }
            Payment payment = paymentRepository.findByOrder(order).orElse(null);
            if (payment != null && payment.getPaymentStatus() == PaymentStatus.PAID) {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
                order.setPaymentStatus(PaymentStatus.REFUNDED);
            }
        }

        // If COD order reaches DELIVERED, mark payment as PAID
        if (newStatus == OrderStatus.DELIVERED && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
            Payment payment = paymentRepository.findByOrder(order).orElse(null);
            if (payment != null) {
                payment.setPaymentStatus(PaymentStatus.PAID);
                paymentRepository.save(payment);
            }
        }

        OrderStatusHistory history = new OrderStatusHistory(
                order,
                newStatus,
                comment != null && !comment.isBlank() ? comment : "Status updated to " + newStatus.getDisplayName(),
                adminName
        );
        historyRepository.save(history);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Page<CustomerOrder> adminSearchOrders(String keyword, OrderStatus status, Pageable pageable) {
        return orderRepository.searchOrders(keyword, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getOrderHistory(CustomerOrder order) {
        return historyRepository.findByOrderOrderByChangedAtAsc(order);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateRevenue() {
        return orderRepository.calculateQualifyingRevenue();
    }

    @Transactional(readOnly = true)
    public long getOrderCount() {
        return orderRepository.count();
    }

    @Transactional(readOnly = true)
    public long getPendingOrderCount() {
        return orderRepository.countByOrderStatus(OrderStatus.PLACED) 
                + orderRepository.countByOrderStatus(OrderStatus.CONFIRMED) 
                + orderRepository.countByOrderStatus(OrderStatus.PROCESSING);
    }

    @Transactional(readOnly = true)
    public long getDeliveredOrderCount() {
        return orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getRecentOrders() {
        return orderRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getOrdersBySeller(User seller) {
        return orderItemRepository.findOrdersBySeller(seller);
    }

    @Transactional(readOnly = true)
    public Page<CustomerOrder> getOrdersBySeller(User seller, Pageable pageable) {
        return orderItemRepository.findOrdersBySeller(seller, pageable);
    }

    @Transactional(readOnly = true)
    public long getSellerOrderCount(User seller) {
        return orderItemRepository.countBySeller(seller);
    }

    @Transactional(readOnly = true)
    public long getSellerPendingOrderCount(User seller) {
        return orderItemRepository.countBySellerAndOrderOrderStatus(seller, OrderStatus.PROCESSING)
                + orderItemRepository.countBySellerAndOrderOrderStatus(seller, OrderStatus.PLACED)
                + orderItemRepository.countBySellerAndOrderOrderStatus(seller, OrderStatus.CONFIRMED);
    }

    @Transactional(readOnly = true)
    public BigDecimal getSellerRevenue(User seller) {
        return orderItemRepository.calculateSellerRevenue(seller);
    }

    public CustomerOrder updateOrderStatus(Long orderId, OrderStatus newStatus, String actorName) {
        return adminUpdateOrderStatus(orderId, newStatus, "Status updated to " + newStatus.getDisplayName(), actorName);
    }

    public void updateSellerOrderStatus(Long orderId, OrderStatus newStatus, User seller) {
        CustomerOrder order = getOrderById(orderId);
        boolean ownsItemInOrder = order.getItems().stream()
                .anyMatch(item -> item.getSeller() != null && item.getSeller().getId().equals(seller.getId()));
        if (!ownsItemInOrder) {
            throw new UnauthorizedAccessException("You do not have permission to manage this order.");
        }
        adminUpdateOrderStatus(orderId, newStatus, "Merchant fulfilment update", "Seller: " + seller.getFullName());
    }
}
