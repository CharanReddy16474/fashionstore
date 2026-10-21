package com.fashionstore.dao.implementation;

import com.fashionstore.dao.PaymentDAO;
import com.fashionstore.model.Payment;
import com.fashionstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PaymentDAOImpl implements PaymentDAO {

    private static final Set<String> VALID_METHODS = Set.of(
            "COD", "UPI", "CARD", "NET_BANKING");

    private static final Set<String> VALID_STATUSES = Set.of(
            "PENDING", "PAID", "FAILED", "REFUNDED", "CANCELLED");

    @Override
    public long createPayment(Payment payment) throws SQLException {
        validatePayment(payment);

        String sql = "INSERT INTO payments "
                + "(order_id, payment_method, amount, payment_status, "
                + "transaction_reference, paid_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, payment.getOrderId());
            statement.setString(2, payment.getPaymentMethod());
            statement.setBigDecimal(3, payment.getAmount());

            statement.setString(
                    4,
                    payment.getPaymentStatus() == null
                            ? "PENDING"
                            : payment.getPaymentStatus());

            statement.setString(
                    5, payment.getTransactionReference());

            if (payment.getPaidAt() == null) {
                statement.setNull(6, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(6, payment.getPaidAt());
            }

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating payment failed: no generated ID returned.");
    }

    @Override
    public Optional<Payment> findById(long paymentId)
            throws SQLException {

        String sql = "SELECT * FROM payments WHERE payment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, paymentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapPayment(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public List<Payment> findByOrderId(long orderId)
            throws SQLException {

        String sql = "SELECT * FROM payments "
                + "WHERE order_id = ? ORDER BY created_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Payment> payments = new ArrayList<>();

                while (resultSet.next()) {
                    payments.add(mapPayment(resultSet));
                }

                return payments;
            }
        }
    }

    @Override
    public Optional<Payment> findByTransactionReference(
            String transactionReference) throws SQLException {

        String sql = "SELECT * FROM payments "
                + "WHERE transaction_reference = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, transactionReference);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapPayment(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public boolean updatePaymentStatus(
            long paymentId, String paymentStatus) throws SQLException {

        validateStatus(paymentStatus);

        String sql = "UPDATE payments "
                + "SET payment_status = ? WHERE payment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, paymentStatus);
            statement.setLong(2, paymentId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean markAsPaid(
            long paymentId,
            String transactionReference,
            Timestamp paidAt) throws SQLException {

        if (paidAt == null) {
            throw new IllegalArgumentException(
                    "Paid timestamp is required.");
        }

        String sql = "UPDATE payments "
                + "SET payment_status = 'PAID', "
                + "transaction_reference = ?, paid_at = ? "
                + "WHERE payment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, transactionReference);
            statement.setTimestamp(2, paidAt);
            statement.setLong(3, paymentId);

            return statement.executeUpdate() > 0;
        }
    }

    private Payment mapPayment(ResultSet resultSet)
            throws SQLException {

        Payment payment = new Payment();

        payment.setPaymentId(resultSet.getLong("payment_id"));
        payment.setOrderId(resultSet.getLong("order_id"));
        payment.setPaymentMethod(resultSet.getString("payment_method"));
        payment.setAmount(resultSet.getBigDecimal("amount"));
        payment.setPaymentStatus(resultSet.getString("payment_status"));
        payment.setTransactionReference(
                resultSet.getString("transaction_reference"));
        payment.setCreatedAt(resultSet.getTimestamp("created_at"));
        payment.setPaidAt(resultSet.getTimestamp("paid_at"));

        return payment;
    }

    private void validatePayment(Payment payment) {
        if (payment == null
                || payment.getAmount() == null
                || payment.getAmount().signum() < 0) {

            throw new IllegalArgumentException(
                    "A payment with a non-negative amount is required.");
        }

        if (!VALID_METHODS.contains(payment.getPaymentMethod())) {
            throw new IllegalArgumentException(
                    "Invalid payment method.");
        }

        if (payment.getPaymentStatus() != null
                && !VALID_STATUSES.contains(
                        payment.getPaymentStatus())) {

            throw new IllegalArgumentException(
                    "Invalid payment status.");
        }
    }

    private void validateStatus(String status) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid payment status.");
        }
    }
}