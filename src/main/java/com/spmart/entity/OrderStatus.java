package com.spmart.entity;

public enum OrderStatus {
    PLACED("Placed"),
    CONFIRMED("Confirmed"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTransitionTo(OrderStatus nextStatus) {
        if (this == CANCELLED || this == DELIVERED) {
            return false;
        }
        return switch (this) {
            case PLACED -> nextStatus == CONFIRMED || nextStatus == CANCELLED;
            case CONFIRMED -> nextStatus == PROCESSING || nextStatus == CANCELLED;
            case PROCESSING -> nextStatus == SHIPPED || nextStatus == CANCELLED;
            case SHIPPED -> nextStatus == OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> nextStatus == DELIVERED;
            default -> false;
        };
    }

    public boolean isCancellableByCustomer() {
        return this == PLACED || this == CONFIRMED || this == PROCESSING;
    }
}
