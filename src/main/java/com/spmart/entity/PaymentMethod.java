package com.spmart.entity;

public enum PaymentMethod {
    COD("Cash on Delivery"),
    UPI("UPI Transfer"),
    GPAY("Google Pay"),
    PAYTM("Paytm"),
    CARD("Debit / Credit Card");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
