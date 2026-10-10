package com.spmart.dto;

import com.spmart.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public class CheckoutDto {

    @NotNull(message = "Please select a delivery address")
    private Long addressId;

    @NotNull(message = "Please select a payment method")
    private PaymentMethod paymentMethod;

    private String idempotencyKey;

    private String notes;

    public CheckoutDto() {
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
