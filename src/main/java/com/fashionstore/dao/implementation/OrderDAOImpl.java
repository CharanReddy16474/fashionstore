package com.fashionstore.dao.implementation;

import com.fashionstore.dao.OrderDAO;
import com.fashionstore.model.Order;
import com.fashionstore.model.OrderItem;
import com.fashionstore.model.Payment;
import com.fashionstore.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OrderDAOImpl implements OrderDAO {

    private static final Set<String> VALID_STATUSES = Set.of(
            "PLACED", "CONFIRMED", "SHIPPED",
            "DELIVERED", "CANCELLED", "RETURNED"
    );

    @Override
    public long createOrder(Order order) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return insertOrder(connection, order);
        }
    }

    @Override
    public long placeOrder(
            Order order,
            List<OrderItem> items,
            Payment payment) throws SQLException {

        if (order == null || payment == null
                || items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Order, payment and at least one order item are required.");
        }

        validateOrderAmounts(order, items);

        if (payment.getAmount() == null
                || payment.getAmount().compareTo(
                        order.getTotalAmount()) != 0) {
            throw new IllegalArgumentException(
                    "Payment amount must equal the order total.");
        }

        String stockSql =
                "UPDATE product_variants "
                + "SET stock_quantity = stock_quantity - ? "
                + "WHERE variant_id = ? AND stock_quantity >= ?";

        String clearCartSql =
                "DELETE ci FROM cart_items ci "
                + "JOIN carts c ON c.cart_id = ci.cart_id "
                + "WHERE c.user_id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                long orderId = insertOrder(connection, order);

                for (OrderItem item : items) {
                    if (item.getVariantId() == null) {
                        throw new SQLException(
                                "A product variant is required when placing an order.");
                    }

                    if (item.getQuantity() <= 0) {
                        throw new SQLException(
                                "Order item quantity must be greater than zero.");
                    }

                    try (PreparedStatement stockStatement =
                                 connection.prepareStatement(stockSql)) {

                        stockStatement.setInt(1, item.getQuantity());
                        stockStatement.setLong(2, item.getVariantId());
                        stockStatement.setInt(3, item.getQuantity());

                        if (stockStatement.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Insufficient stock for variant ID "
                                            + item.getVariantId());
                        }
                    }

                    insertOrderItem(connection, orderId, item);
                }

                insertPayment(connection, orderId, payment);

                try (PreparedStatement clearCart =
                             connection.prepareStatement(clearCartSql)) {

                    clearCart.setLong(1, order.getUserId());
                    clearCart.executeUpdate();
                }

                connection.commit();
                return orderId;

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public Optional<Order> findById(long orderId)
            throws SQLException {

        return findOne(
                "SELECT * FROM orders WHERE order_id = ?",
                orderId);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber)
            throws SQLException {

        String sql = "SELECT * FROM orders WHERE order_number = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, orderNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapOrder(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Order> findByIdAndUserId(
            long orderId, long userId) throws SQLException {

        String sql = "SELECT * FROM orders "
                + "WHERE order_id = ? AND user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);
            statement.setLong(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapOrder(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public List<Order> findByUserId(long userId)
            throws SQLException {

        String sql = "SELECT * FROM orders "
                + "WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Order> orders = new ArrayList<>();

                while (resultSet.next()) {
                    orders.add(mapOrder(resultSet));
                }

                return orders;
            }
        }
    }

    @Override
    public List<Order> findByUserIdAndStatus(
            long userId, String orderStatus) throws SQLException {

        validateStatus(orderStatus);

        String sql = "SELECT * FROM orders "
                + "WHERE user_id = ? AND order_status = ? "
                + "ORDER BY created_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(2, orderStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Order> orders = new ArrayList<>();

                while (resultSet.next()) {
                    orders.add(mapOrder(resultSet));
                }

                return orders;
            }
        }
    }

    @Override
    public boolean updateOrderStatus(
            long orderId, String orderStatus) throws SQLException {

        validateStatus(orderStatus);

        String sql = "UPDATE orders "
                + "SET order_status = ? WHERE order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, orderStatus);
            statement.setLong(2, orderId);

            return statement.executeUpdate() > 0;
        }
    }

    private long insertOrder(
            Connection connection, Order order) throws SQLException {

        String sql = "INSERT INTO orders "
                + "(order_number, user_id, shipping_name, shipping_phone, "
                + "shipping_address, subtotal, discount_amount, "
                + "shipping_fee, total_amount, order_status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, order.getOrderNumber());
            statement.setLong(2, order.getUserId());
            statement.setString(3, order.getShippingName());
            statement.setString(4, order.getShippingPhone());
            statement.setString(5, order.getShippingAddress());
            statement.setBigDecimal(6, order.getSubtotal());
            statement.setBigDecimal(7, order.getDiscountAmount());
            statement.setBigDecimal(8, order.getShippingFee());
            statement.setBigDecimal(9, order.getTotalAmount());

            statement.setString(
                    10,
                    order.getOrderStatus() == null
                            ? "PLACED"
                            : order.getOrderStatus());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating order failed: no generated ID returned.");
    }

    private void insertOrderItem(
            Connection connection,
            long orderId,
            OrderItem item) throws SQLException {

        String sql = "INSERT INTO order_items "
                + "(order_id, variant_id, product_name, size, color, "
                + "unit_price, quantity, line_total) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        BigDecimal expectedLineTotal =
                item.getUnitPrice().multiply(
                        BigDecimal.valueOf(item.getQuantity()));

        if (item.getLineTotal() == null
                || expectedLineTotal.compareTo(
                        item.getLineTotal()) != 0) {
            throw new SQLException(
                    "Order item line total does not match unit price multiplied by quantity.");
        }

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);

            if (item.getVariantId() == null) {
                statement.setNull(2, java.sql.Types.BIGINT);
            } else {
                statement.setLong(2, item.getVariantId());
            }

            statement.setString(3, item.getProductName());
            statement.setString(4, item.getSize());
            statement.setString(5, item.getColor());
            statement.setBigDecimal(6, item.getUnitPrice());
            statement.setInt(7, item.getQuantity());
            statement.setBigDecimal(8, item.getLineTotal());

            statement.executeUpdate();
        }
    }

    private void insertPayment(
            Connection connection,
            long orderId,
            Payment payment) throws SQLException {

        String sql = "INSERT INTO payments "
                + "(order_id, payment_method, amount, payment_status, "
                + "transaction_reference, paid_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);
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
        }
    }

    private void validateOrderAmounts(
            Order order, List<OrderItem> items) {

        if (order.getSubtotal() == null
                || order.getDiscountAmount() == null
                || order.getShippingFee() == null
                || order.getTotalAmount() == null) {
            throw new IllegalArgumentException(
                    "Order monetary values are required.");
        }

        BigDecimal calculatedSubtotal = BigDecimal.ZERO;

        for (OrderItem item : items) {
            if (item.getUnitPrice() == null
                    || item.getLineTotal() == null
                    || item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Order items require a price, line total and positive quantity.");
            }

            BigDecimal calculatedLine =
                    item.getUnitPrice().multiply(
                            BigDecimal.valueOf(item.getQuantity()));

            if (calculatedLine.compareTo(item.getLineTotal()) != 0) {
                throw new IllegalArgumentException(
                        "An order item line total is incorrect.");
            }

            calculatedSubtotal =
                    calculatedSubtotal.add(item.getLineTotal());
        }

        if (calculatedSubtotal.compareTo(order.getSubtotal()) != 0) {
            throw new IllegalArgumentException(
                    "Order subtotal does not match the item totals.");
        }

        BigDecimal calculatedTotal =
                order.getSubtotal()
                        .subtract(order.getDiscountAmount())
                        .add(order.getShippingFee());

        if (calculatedTotal.compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalArgumentException(
                    "Order total is incorrect.");
        }
    }

    private Optional<Order> findOne(
            String sql, long orderId) throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapOrder(resultSet))
                        : Optional.empty();
            }
        }
    }

    private Order mapOrder(ResultSet resultSet) throws SQLException {
        Order order = new Order();

        order.setOrderId(resultSet.getLong("order_id"));
        order.setOrderNumber(resultSet.getString("order_number"));
        order.setUserId(resultSet.getLong("user_id"));
        order.setShippingName(resultSet.getString("shipping_name"));
        order.setShippingPhone(resultSet.getString("shipping_phone"));
        order.setShippingAddress(resultSet.getString("shipping_address"));
        order.setSubtotal(resultSet.getBigDecimal("subtotal"));
        order.setDiscountAmount(
                resultSet.getBigDecimal("discount_amount"));
        order.setShippingFee(resultSet.getBigDecimal("shipping_fee"));
        order.setTotalAmount(resultSet.getBigDecimal("total_amount"));
        order.setOrderStatus(resultSet.getString("order_status"));
        order.setCreatedAt(resultSet.getTimestamp("created_at"));
        order.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return order;
    }

    private void validateStatus(String status) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid order status: " + status);
        }
    }
}