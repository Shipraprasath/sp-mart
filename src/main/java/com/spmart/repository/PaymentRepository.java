package com.spmart.repository;

import com.spmart.entity.CustomerOrder;
import com.spmart.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(CustomerOrder order);
    Optional<Payment> findByTransactionReference(String transactionReference);
}
