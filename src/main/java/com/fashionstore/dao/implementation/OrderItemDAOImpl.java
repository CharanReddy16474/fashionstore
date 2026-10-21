package com.fashionstore.dao.implementation;

import com.fashionstore.dao.OrderItemDAO;
import com.fashionstore.model.OrderItem;
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

public class OrderItemDAOImpl implements OrderItemDAO {

    @Override
    public long createOrderItem(OrderItem orderItem)
            throws SQLException {

        if (orderItem.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero.");
        }

        if (orderItem.getUnitPrice() == null
                || orderItem.getLineTotal() == null
                || orderItem.getUnitPrice()
                        .multiply(BigDecimal.valueOf(
                                orderItem.getQuantity()))
                        .compareTo(orderItem.getLineTotal()) != 0) {

            throw new IllegalArgumentException(
                    "Line total must equal unit price multiplied by quantity.");
        }

        String sql = "INSERT INTO order_items "
                + "(order_id, variant_id, product_name, size, color, "
                + "unit_price, quantity, line_total) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, orderItem.getOrderId());

            if (orderItem.getVariantId() == null) {
                statement.setNull(2, java.sql.Types.BIGINT);
            } else {
                statement.setLong(2, orderItem.getVariantId());
            }

            statement.setString(3, orderItem.getProductName());
            statement.setString(4, orderItem.getSize());
            statement.setString(5, orderItem.getColor());
            statement.setBigDecimal(6, orderItem.getUnitPrice());
            statement.setInt(7, orderItem.getQuantity());
            statement.setBigDecimal(8, orderItem.getLineTotal());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating order item failed: no generated ID returned.");
    }

    @Override
    public Optional<OrderItem> findById(long orderItemId)
            throws SQLException {

        String sql = "SELECT * FROM order_items "
                + "WHERE order_item_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, orderItemId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapOrderItem(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public List<OrderItem> findByOrderId(long orderId)
            throws SQLException {

        String sql = "SELECT * FROM order_items "
                + "WHERE order_id = ? ORDER BY order_item_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();

                while (resultSet.next()) {
                    items.add(mapOrderItem(resultSet));
                }

                return items;
            }
        }
    }

    @Override
    public long countByOrderId(long orderId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM order_items "
                + "WHERE order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private OrderItem mapOrderItem(ResultSet resultSet)
            throws SQLException {

        OrderItem item = new OrderItem();

        item.setOrderItemId(resultSet.getLong("order_item_id"));
        item.setOrderId(resultSet.getLong("order_id"));
        item.setVariantId(
                resultSet.getObject("variant_id", Long.class));
        item.setProductName(resultSet.getString("product_name"));
        item.setSize(resultSet.getString("size"));
        item.setColor(resultSet.getString("color"));
        item.setUnitPrice(resultSet.getBigDecimal("unit_price"));
        item.setQuantity(resultSet.getInt("quantity"));
        item.setLineTotal(resultSet.getBigDecimal("line_total"));

        return item;
    }
}