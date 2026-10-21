package com.fashionstore.dao;

import com.fashionstore.model.OrderItem;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface OrderItemDAO {

    long createOrderItem(OrderItem orderItem) throws SQLException;

    Optional<OrderItem> findById(long orderItemId)
            throws SQLException;

    List<OrderItem> findByOrderId(long orderId) throws SQLException;

    long countByOrderId(long orderId) throws SQLException;
}