package com.fashionstore.model;

import java.math.BigDecimal;

public class OrderItem {

    private long orderItemId;
    private long orderId;
    private Long variantId;
    private String productName;
    private String size;
    private String color;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;

    public OrderItem() {
    }

    public OrderItem(long orderItemId, long orderId, Long variantId,
                     String productName, String size, String color,
                     BigDecimal unitPrice, int quantity,
                     BigDecimal lineTotal) {

        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.variantId = variantId;
        this.productName = productName;
        this.size = size;
        this.color = color;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}