package com.fashionstore.dao.implementation;

import com.fashionstore.dao.CartItemDAO;
import com.fashionstore.model.CartItem;
import com.fashionstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartItemDAOImpl implements CartItemDAO {

    @Override
    public long createCartItem(CartItem cartItem) throws SQLException {
        if (cartItem.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero.");
        }

        String sql = "INSERT INTO cart_items "
                + "(cart_id, variant_id, quantity) VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, cartItem.getCartId());
            statement.setLong(2, cartItem.getVariantId());
            statement.setInt(3, cartItem.getQuantity());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating cart item failed: no generated ID returned.");
    }

    @Override
    public Optional<CartItem> findById(long cartItemId)
            throws SQLException {

        String sql = "SELECT * FROM cart_items WHERE cart_item_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartItemId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapCartItem(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public List<CartItem> findByCartId(long cartId) throws SQLException {
        String sql = "SELECT * FROM cart_items "
                + "WHERE cart_id = ? ORDER BY added_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<CartItem> items = new ArrayList<>();

                while (resultSet.next()) {
                    items.add(mapCartItem(resultSet));
                }

                return items;
            }
        }
    }

    @Override
    public Optional<CartItem> findByCartAndVariant(
            long cartId, long variantId) throws SQLException {

        String sql = "SELECT * FROM cart_items "
                + "WHERE cart_id = ? AND variant_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartId);
            statement.setLong(2, variantId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapCartItem(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public boolean addOrIncreaseQuantity(
            long cartId, long variantId, int quantity)
            throws SQLException {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero.");
        }

        String stockSql = "SELECT stock_quantity "
                + "FROM product_variants WHERE variant_id = ? FOR UPDATE";

        String itemSql = "SELECT cart_item_id, quantity "
                + "FROM cart_items WHERE cart_id = ? AND variant_id = ? "
                + "FOR UPDATE";

        String updateSql = "UPDATE cart_items "
                + "SET quantity = ? WHERE cart_item_id = ?";

        String insertSql = "INSERT INTO cart_items "
                + "(cart_id, variant_id, quantity) VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                int stock;

                try (PreparedStatement statement =
                             connection.prepareStatement(stockSql)) {

                    statement.setLong(1, variantId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException(
                                    "Product variant not found.");
                        }

                        stock = resultSet.getInt("stock_quantity");
                    }
                }

                long itemId = -1;
                int oldQuantity = 0;

                try (PreparedStatement statement =
                             connection.prepareStatement(itemSql)) {

                    statement.setLong(1, cartId);
                    statement.setLong(2, variantId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            itemId = resultSet.getLong("cart_item_id");
                            oldQuantity = resultSet.getInt("quantity");
                        }
                    }
                }

                int newQuantity = oldQuantity + quantity;

                if (newQuantity > stock) {
                    throw new SQLException(
                            "Requested quantity exceeds available stock.");
                }

                if (itemId >= 0) {
                    try (PreparedStatement statement =
                                 connection.prepareStatement(updateSql)) {

                        statement.setInt(1, newQuantity);
                        statement.setLong(2, itemId);
                        statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement =
                                 connection.prepareStatement(insertSql)) {

                        statement.setLong(1, cartId);
                        statement.setLong(2, variantId);
                        statement.setInt(3, quantity);
                        statement.executeUpdate();
                    }
                }

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public boolean updateQuantity(long cartItemId, int quantity)
            throws SQLException {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero.");
        }

        String selectSql = "SELECT pv.stock_quantity "
                + "FROM cart_items ci "
                + "JOIN product_variants pv "
                + "ON pv.variant_id = ci.variant_id "
                + "WHERE ci.cart_item_id = ? FOR UPDATE";

        String updateSql = "UPDATE cart_items "
                + "SET quantity = ? WHERE cart_item_id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                int stock;

                try (PreparedStatement statement =
                             connection.prepareStatement(selectSql)) {

                    statement.setLong(1, cartItemId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }

                        stock = resultSet.getInt("stock_quantity");
                    }
                }

                if (quantity > stock) {
                    throw new SQLException(
                            "Requested quantity exceeds available stock.");
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {

                    statement.setInt(1, quantity);
                    statement.setLong(2, cartItemId);
                    statement.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public boolean removeCartItem(long cartItemId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_item_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartItemId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean clearCart(long cartId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartId);
            statement.executeUpdate();

            return true;
        }
    }

    private CartItem mapCartItem(ResultSet resultSet)
            throws SQLException {

        CartItem item = new CartItem();

        item.setCartItemId(resultSet.getLong("cart_item_id"));
        item.setCartId(resultSet.getLong("cart_id"));
        item.setVariantId(resultSet.getLong("variant_id"));
        item.setQuantity(resultSet.getInt("quantity"));
        item.setAddedAt(resultSet.getTimestamp("added_at"));
        item.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return item;
    }
}