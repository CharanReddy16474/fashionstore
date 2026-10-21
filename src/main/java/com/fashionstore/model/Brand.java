package com.fashionstore.model;

import java.sql.Timestamp;

public class Brand {

    private int brandId;
    private String brandName;
    private boolean active;
    private Timestamp createdAt;

    // Default constructor
    public Brand() {
    }

    // Parameterized constructor
    public Brand(int brandId, String brandName,
                 boolean active, Timestamp createdAt) {

        this.brandId = brandId;
        this.brandName = brandName;
        this.active = active;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
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
}