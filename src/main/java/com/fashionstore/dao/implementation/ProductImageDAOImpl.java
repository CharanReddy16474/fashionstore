package com.fashionstore.dao.implementation;

import com.fashionstore.dao.ProductImageDAO;
import com.fashionstore.model.ProductImage;
import com.fashionstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductImageDAOImpl implements ProductImageDAO {

    @Override
    public long createImage(ProductImage image) throws SQLException {
        String sql = "INSERT INTO product_images "
                + "(product_id, image_url, is_primary, display_order) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, image.getProductId());
            statement.setString(2, image.getImageUrl());
            statement.setBoolean(3, image.isPrimary());
            statement.setInt(4, image.getDisplayOrder());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating product image failed: no generated ID returned.");
    }

    @Override
    public Optional<ProductImage> findById(long imageId)
            throws SQLException {

        String sql = "SELECT * FROM product_images WHERE image_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, imageId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapImage(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public List<ProductImage> findByProductId(long productId)
            throws SQLException {

        String sql = "SELECT * FROM product_images "
                + "WHERE product_id = ? ORDER BY display_order, image_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<ProductImage> images = new ArrayList<>();

                while (resultSet.next()) {
                    images.add(mapImage(resultSet));
                }

                return images;
            }
        }
    }

    @Override
    public Optional<ProductImage> findPrimaryByProductId(long productId)
            throws SQLException {

        String sql = "SELECT * FROM product_images "
                + "WHERE product_id = ? AND is_primary = TRUE "
                + "ORDER BY image_id LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapImage(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public boolean updateImage(ProductImage image) throws SQLException {
        String sql = "UPDATE product_images SET "
                + "image_url = ?, is_primary = ?, display_order = ? "
                + "WHERE image_id = ? AND product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, image.getImageUrl());
            statement.setBoolean(2, image.isPrimary());
            statement.setInt(3, image.getDisplayOrder());
            statement.setLong(4, image.getImageId());
            statement.setLong(5, image.getProductId());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean setPrimaryImage(long productId, long imageId)
            throws SQLException {

        String verifySql = "SELECT 1 FROM product_images "
                + "WHERE product_id = ? AND image_id = ?";

        String clearSql = "UPDATE product_images "
                + "SET is_primary = FALSE WHERE product_id = ?";

        String setSql = "UPDATE product_images "
                + "SET is_primary = TRUE "
                + "WHERE product_id = ? AND image_id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement verify =
                             connection.prepareStatement(verifySql)) {

                    verify.setLong(1, productId);
                    verify.setLong(2, imageId);

                    try (ResultSet resultSet = verify.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement clear =
                             connection.prepareStatement(clearSql)) {

                    clear.setLong(1, productId);
                    clear.executeUpdate();
                }

                try (PreparedStatement set =
                             connection.prepareStatement(setSql)) {

                    set.setLong(1, productId);
                    set.setLong(2, imageId);
                    set.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public boolean deleteImage(long imageId) throws SQLException {
        String sql = "DELETE FROM product_images WHERE image_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, imageId);

            return statement.executeUpdate() > 0;
        }
    }

    private ProductImage mapImage(ResultSet resultSet)
            throws SQLException {

        ProductImage image = new ProductImage();

        image.setImageId(resultSet.getLong("image_id"));
        image.setProductId(resultSet.getLong("product_id"));
        image.setImageUrl(resultSet.getString("image_url"));
        image.setPrimary(resultSet.getBoolean("is_primary"));
        image.setDisplayOrder(resultSet.getInt("display_order"));
        image.setCreatedAt(resultSet.getTimestamp("created_at"));

        return image;
    }
}