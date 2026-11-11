package com.spmart.repository;

import com.spmart.entity.CustomerOrder;
import com.spmart.entity.OrderStatus;
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
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findByOrderNumber(String orderNumber);

    Optional<CustomerOrder> findByOrderNumberAndUser(String orderNumber, User user);

    List<CustomerOrder> findByUserOrderByCreatedAtDesc(User user);

    Page<CustomerOrder> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<CustomerOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<CustomerOrder> findByOrderStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    long countByOrderStatus(OrderStatus status);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT o FROM CustomerOrder o WHERE " +
           "(:status IS NULL OR o.orderStatus = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(o.recipientName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(o.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<CustomerOrder> searchOrders(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM CustomerOrder o " +
           "WHERE o.orderStatus <> com.spmart.entity.OrderStatus.CANCELLED " +
           "AND (o.paymentStatus = com.spmart.entity.PaymentStatus.PAID " +
           "     OR (o.paymentMethod = com.spmart.entity.PaymentMethod.COD AND o.orderStatus = com.spmart.entity.OrderStatus.DELIVERED))")
    BigDecimal calculateQualifyingRevenue();

    List<CustomerOrder> findTop5ByOrderByCreatedAtDesc();
}
