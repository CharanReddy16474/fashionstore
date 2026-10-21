package com.fashionstore.dao;

import com.fashionstore.model.CartItem;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CartItemDAO {

    long createCartItem(CartItem cartItem) throws SQLException;

    Optional<CartItem> findById(long cartItemId)
            throws SQLException;

    List<CartItem> findByCartId(long cartId) throws SQLException;

    Optional<CartItem> findByCartAndVariant(
            long cartId, long variantId) throws SQLException;

    boolean addOrIncreaseQuantity(
            long cartId, long variantId, int quantity)
            throws SQLException;

    boolean updateQuantity(long cartItemId, int quantity)
            throws SQLException;

    boolean removeCartItem(long cartItemId) throws SQLException;

    boolean clearCart(long cartId) throws SQLException;
}