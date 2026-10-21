package com.fashionstore.dao;

import com.fashionstore.model.Order;
import com.fashionstore.model.OrderItem;
import com.fashionstore.model.Payment;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {

    long createOrder(Order order) throws SQLException;

    long placeOrder(
            Order order,
            List<OrderItem> items,
            Payment payment
    ) throws SQLException;

    Optional<Order> findById(long orderId) throws SQLException;

    Optional<Order> findByOrderNumber(String orderNumber)
            throws SQLException;

    Optional<Order> findByIdAndUserId(long orderId, long userId)
            throws SQLException;

    List<Order> findByUserId(long userId) throws SQLException;

    List<Order> findByUserIdAndStatus(
            long userId, String orderStatus)
            throws SQLException;

    boolean updateOrderStatus(long orderId, String orderStatus)
            throws SQLException;
}