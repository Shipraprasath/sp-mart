package com.spmart.service;

import com.spmart.entity.*;
import com.spmart.exception.BusinessRuleException;
import com.spmart.exception.ResourceNotFoundException;
import com.spmart.repository.CustomerOrderRepository;
import com.spmart.repository.OrderStatusHistoryRepository;
import com.spmart.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerOrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    @Value("${spmart.merchant.upi-id:spmart.pay@okaxis}")
    private String merchantUpiId;

    public PaymentService(PaymentRepository paymentRepository,
                          CustomerOrderRepository orderRepository,
                          OrderStatusHistoryRepository historyRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrder(CustomerOrder order) {
        return paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for order: " + order.getOrderNumber()));
    }

    public String generateUpiUri(CustomerOrder order) {
        String pa = URLEncoder.encode(merchantUpiId, StandardCharsets.UTF_8);
        String pn = URLEncoder.encode("SP-Mart Online", StandardCharsets.UTF_8);
        String am = order.getFinalAmount().toPlainString();
        String tr = URLEncoder.encode(order.getOrderNumber(), StandardCharsets.UTF_8);
        String tn = URLEncoder.encode("Order " + order.getOrderNumber(), StandardCharsets.UTF_8);

        return String.format("upi://pay?pa=%s&pn=%s&am=%s&tr=%s&tn=%s&cu=INR", pa, pn, am, tr, tn);
    }

    public Payment completeDemoPayment(CustomerOrder order, String simulatedTxnId, boolean isSuccess) {
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot process payment for a cancelled order.");
        }

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for order: " + order.getOrderNumber()));

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            return payment; // Idempotent
        }

        if (isSuccess) {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setTransactionReference(simulatedTxnId != null && !simulatedTxnId.isBlank() ? simulatedTxnId : payment.getTransactionReference());
            payment.setGatewayResponse("Simulated Payment Gateway: Transaction Approved [SUCCESS]");
            order.setPaymentStatus(PaymentStatus.PAID);
            if (order.getOrderStatus() == OrderStatus.PLACED) {
                order.setOrderStatus(OrderStatus.CONFIRMED);
                OrderStatusHistory history = new OrderStatusHistory(
                        order,
                        OrderStatus.CONFIRMED,
                        "Payment verified online. Order auto-confirmed.",
                        "Payment Gateway"
                );
                historyRepository.save(history);
            }
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setGatewayResponse("Simulated Payment Gateway: Transaction Declined [USER_ABORTED_OR_DECLINED]");
            order.setPaymentStatus(PaymentStatus.FAILED);
        }

        orderRepository.save(order);
        return paymentRepository.save(payment);
    }

    public String getMerchantUpiId() {
        return merchantUpiId;
    }
}
