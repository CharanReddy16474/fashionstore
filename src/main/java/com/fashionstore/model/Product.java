package com.fashionstore.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product {

    private long productId;
    private String productName;
    private String description;
    private int categoryId;
    private Integer brandId;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default constructor
    public Product() {
    }

    // Parameterized constructor
    public Product(long productId, String productName,
                   String description, int categoryId,
                   Integer brandId, BigDecimal price,
                   BigDecimal discountPrice, boolean active,
                   Timestamp createdAt, Timestamp updatedAt) {

        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.price = price;
        this.discountPrice = discountPrice;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(BigDecimal discountPrice) {
        this.discountPrice = discountPrice;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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