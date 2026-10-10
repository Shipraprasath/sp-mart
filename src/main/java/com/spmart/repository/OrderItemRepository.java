package com.spmart.repository;

import com.spmart.entity.CustomerOrder;
import com.spmart.entity.OrderItem;
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

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findBySeller(User seller);

    List<OrderItem> findBySellerOrderByOrderCreatedAtDesc(User seller);

    long countBySeller(User seller);

    long countBySellerAndOrderOrderStatus(User seller, OrderStatus status);

    @Query("SELECT DISTINCT oi.order FROM OrderItem oi WHERE oi.seller = :seller ORDER BY oi.order.createdAt DESC")
    List<CustomerOrder> findOrdersBySeller(@Param("seller") User seller);

    @Query("SELECT DISTINCT oi.order FROM OrderItem oi WHERE oi.seller = :seller ORDER BY oi.order.createdAt DESC")
    Page<CustomerOrder> findOrdersBySeller(@Param("seller") User seller, Pageable pageable);

    @Query("SELECT COALESCE(SUM(oi.lineTotal), 0) FROM OrderItem oi WHERE oi.seller = :seller AND oi.order.orderStatus <> com.spmart.entity.OrderStatus.CANCELLED")
    BigDecimal calculateSellerRevenue(@Param("seller") User seller);
}
