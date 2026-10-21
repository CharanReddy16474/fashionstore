package com.fashionstore.dao;

import com.fashionstore.model.Cart;
import java.sql.SQLException;
import java.util.Optional;

public interface CartDAO {

    long createCart(Cart cart) throws SQLException;

    Optional<Cart> findById(long cartId) throws SQLException;

    Optional<Cart> findByUserId(long userId) throws SQLException;

    boolean deleteCart(long cartId) throws SQLException;
}