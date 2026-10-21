package com.fashionstore.model;

import java.sql.Timestamp;

public class Category {

    private int categoryId;
    private String categoryName;
    private String categorySlug;
    private Integer parentCategoryId;
    private String description;
    private boolean active;
    private Timestamp createdAt;

    // Default constructor
    public Category() {
    }

    // Parameterized constructor
    public Category(int categoryId, String categoryName,
                    String categorySlug, Integer parentCategoryId,
                    String description, boolean active,
                    Timestamp createdAt) {

        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categorySlug = categorySlug;
        this.parentCategoryId = parentCategoryId;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public void setCategorySlug(String categorySlug) {
        this.categorySlug = categorySlug;
    }

    public Integer getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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