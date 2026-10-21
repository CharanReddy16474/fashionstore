package com.fashionstore.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Payment {

    private long paymentId;
    private long orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private String paymentStatus;
    private String transactionReference;
    private Timestamp createdAt;
    private Timestamp paidAt;

    public Payment() {
    }

    public Payment(long paymentId, long orderId,
                   String paymentMethod, BigDecimal amount,
                   String paymentStatus, String transactionReference,
                   Timestamp createdAt, Timestamp paidAt) {

        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.transactionReference = transactionReference;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(long paymentId) {
        this.paymentId = paymentId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }
}