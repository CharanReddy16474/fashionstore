package com.fashionstore.dao.implementation;

import com.fashionstore.dao.ProductVariantDAO;
import com.fashionstore.model.ProductVariant;
import com.fashionstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductVariantDAOImpl implements ProductVariantDAO {

    @Override
    public long createVariant(ProductVariant variant)
            throws SQLException {

        validateStock(variant.getStockQuantity());

        String sql = "INSERT INTO product_variants "
                + "(product_id, sku, size, color, stock_quantity) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, variant.getProductId());
            statement.setString(2, variant.getSku());
            statement.setString(3, variant.getSize());
            statement.setString(4, variant.getColor());
            statement.setInt(5, variant.getStockQuantity());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating product variant failed: no generated ID returned.");
    }

    @Override
    public Optional<ProductVariant> findById(long variantId)
            throws SQLException {

        String sql = "SELECT * FROM product_variants WHERE variant_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, variantId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapVariant(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public List<ProductVariant> findByProductId(long productId)
            throws SQLException {

        String sql = "SELECT * FROM product_variants "
                + "WHERE product_id = ? ORDER BY size, color";

        return findMany(sql, productId);
    }

    @Override
    public List<ProductVariant> findAvailableByProductId(long productId)
            throws SQLException {

        String sql = "SELECT * FROM product_variants "
                + "WHERE product_id = ? AND stock_quantity > 0 "
                + "ORDER BY size, color";

        return findMany(sql, productId);
    }

    @Override
    public Optional<ProductVariant> findByProductSizeAndColor(
            long productId, String size, String color)
            throws SQLException {

        String sql = "SELECT * FROM product_variants "
                + "WHERE product_id = ? AND size = ? AND color = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);
            statement.setString(2, size);
            statement.setString(3, color);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapVariant(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public boolean updateVariant(ProductVariant variant)
            throws SQLException {

        validateStock(variant.getStockQuantity());

        String sql = "UPDATE product_variants SET "
                + "product_id = ?, sku = ?, size = ?, color = ?, "
                + "stock_quantity = ? WHERE variant_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, variant.getProductId());
            statement.setString(2, variant.getSku());
            statement.setString(3, variant.getSize());
            statement.setString(4, variant.getColor());
            statement.setInt(5, variant.getStockQuantity());
            statement.setLong(6, variant.getVariantId());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStockQuantity(long variantId, int quantity)
            throws SQLException {

        validateStock(quantity);

        String sql = "UPDATE product_variants "
                + "SET stock_quantity = ? WHERE variant_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setLong(2, variantId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean decreaseStockIfAvailable(
            long variantId, int quantity) throws SQLException {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero.");
        }

        String sql = "UPDATE product_variants "
                + "SET stock_quantity = stock_quantity - ? "
                + "WHERE variant_id = ? AND stock_quantity >= ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setLong(2, variantId);
            statement.setInt(3, quantity);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean increaseStock(long variantId, int quantity)
            throws SQLException {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero.");
        }

        String sql = "UPDATE product_variants "
                + "SET stock_quantity = stock_quantity + ? "
                + "WHERE variant_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setLong(2, variantId);

            return statement.executeUpdate() > 0;
        }
    }

    private List<ProductVariant> findMany(
            String sql, long productId) throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<ProductVariant> variants = new ArrayList<>();

                while (resultSet.next()) {
                    variants.add(mapVariant(resultSet));
                }

                return variants;
            }
        }
    }

    private ProductVariant mapVariant(ResultSet resultSet)
            throws SQLException {

        ProductVariant variant = new ProductVariant();

        variant.setVariantId(resultSet.getLong("variant_id"));
        variant.setProductId(resultSet.getLong("product_id"));
        variant.setSku(resultSet.getString("sku"));
        variant.setSize(resultSet.getString("size"));
        variant.setColor(resultSet.getString("color"));
        variant.setStockQuantity(resultSet.getInt("stock_quantity"));
        variant.setCreatedAt(resultSet.getTimestamp("created_at"));
        variant.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return variant;
    }

    private void validateStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative.");
        }
    }
}