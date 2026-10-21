package com.fashionstore.dao.implementation;

import com.fashionstore.dao.CartDAO;
import com.fashionstore.model.Cart;
import com.fashionstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class CartDAOImpl implements CartDAO {

    @Override
    public long createCart(Cart cart) throws SQLException {
        String sql = "INSERT INTO carts (user_id) VALUES (?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, cart.getUserId());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating cart failed: no generated ID returned.");
    }

    @Override
    public Optional<Cart> findById(long cartId) throws SQLException {
        String sql = "SELECT * FROM carts WHERE cart_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapCart(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Cart> findByUserId(long userId) throws SQLException {
        String sql = "SELECT * FROM carts WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapCart(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public boolean deleteCart(long cartId) throws SQLException {
        String sql = "DELETE FROM carts WHERE cart_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartId);

            return statement.executeUpdate() > 0;
        }
    }

    private Cart mapCart(ResultSet resultSet) throws SQLException {
        Cart cart = new Cart();

        cart.setCartId(resultSet.getLong("cart_id"));
        cart.setUserId(resultSet.getLong("user_id"));
        cart.setCreatedAt(resultSet.getTimestamp("created_at"));
        cart.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return cart;
    }
}