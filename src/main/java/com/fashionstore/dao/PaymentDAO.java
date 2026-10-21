package com.fashionstore.dao;

import com.fashionstore.model.Payment;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface PaymentDAO {

    long createPayment(Payment payment) throws SQLException;

    Optional<Payment> findById(long paymentId) throws SQLException;

    List<Payment> findByOrderId(long orderId) throws SQLException;

    Optional<Payment> findByTransactionReference(
            String transactionReference) throws SQLException;

    boolean updatePaymentStatus(
            long paymentId, String paymentStatus)
            throws SQLException;

    boolean markAsPaid(
            long paymentId,
            String transactionReference,
            Timestamp paidAt
    ) throws SQLException;
}