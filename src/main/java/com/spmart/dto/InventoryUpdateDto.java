package com.spmart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class InventoryUpdateDto {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer newStock;

    public InventoryUpdateDto() {
    }

    public InventoryUpdateDto(Long productId, Integer newStock) {
        this.productId = productId;
        this.newStock = newStock;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getNewStock() {
        return newStock;
    }

    public void setNewStock(Integer newStock) {
        this.newStock = newStock;
    }
}
