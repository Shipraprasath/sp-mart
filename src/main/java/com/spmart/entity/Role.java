package com.spmart.entity;

public enum Role {
    ROLE_CUSTOMER("Buyer"),
    ROLE_SELLER("Seller"),
    ROLE_ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
