package com.fashionstore.model;

import java.sql.Timestamp;

public class ProductVariant {

    private long variantId;
    private long productId;
    private String sku;
    private String size;
    private String color;
    private int stockQuantity;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default constructor
    public ProductVariant() {
    }

    // Parameterized constructor
    public ProductVariant(long variantId, long productId,
                          String sku, String size, String color,
                          int stockQuantity, Timestamp createdAt,
                          Timestamp updatedAt) {

        this.variantId = variantId;
        this.productId = productId;
        this.sku = sku;
        this.size = size;
        this.color = color;
        this.stockQuantity = stockQuantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public long getVariantId() {
        return variantId;
    }

    public void setVariantId(long variantId) {
        this.variantId = variantId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}