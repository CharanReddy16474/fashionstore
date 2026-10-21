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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                "Creating product image failed: no generated ID returned."
        );
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
                + "WHERE product_id = ? "
                + "ORDER BY display_order, image_id";

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

    /**
     * Load images for multiple products with one query.
     * Prefer a usable primary image; otherwise use the first usable
     * fallback image ordered by display_order and image_id.
     */
    public Map<Long, String> findFirstUsableImageUrlByProductIds(
            List<Long> productIds) throws SQLException {

        Map<Long, ProductImage> primaryImages = new HashMap<>();
        Map<Long, ProductImage> fallbackImages = new HashMap<>();
        Map<Long, String> result = new HashMap<>();

        if (productIds == null || productIds.isEmpty()) {
            return result;
        }

        String placeholders = String.join(
                ",",
                Collections.nCopies(productIds.size(), "?")
        );

        String sql = "SELECT * FROM product_images "
                + "WHERE product_id IN (" + placeholders + ") "
                + "ORDER BY product_id, display_order, image_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (int i = 0; i < productIds.size(); i++) {
                statement.setLong(i + 1, productIds.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    ProductImage image = mapImage(resultSet);
                    long productId = image.getProductId();

                    if (image.isPrimary()) {

                        ProductImage currentPrimary =
                                primaryImages.get(productId);

                        if (currentPrimary == null
                                || image.getImageId()
                                < currentPrimary.getImageId()) {

                            primaryImages.put(productId, image);
                        }
                    }

                    String imageUrl = image.getImageUrl();

                    if (imageUrl != null && !imageUrl.isBlank()) {

                        ProductImage currentFallback =
                                fallbackImages.get(productId);

                        if (currentFallback == null
                                || image.getDisplayOrder()
                                < currentFallback.getDisplayOrder()
                                || (image.getDisplayOrder()
                                == currentFallback.getDisplayOrder()
                                && image.getImageId()
                                < currentFallback.getImageId())) {

                            fallbackImages.put(productId, image);
                        }
                    }
                }
            }
        }

        for (Long productId : productIds) {

            ProductImage primary = primaryImages.get(productId);

            if (primary != null
                    && primary.getImageUrl() != null
                    && !primary.getImageUrl().isBlank()) {

                result.put(productId, primary.getImageUrl());

            } else {

                ProductImage fallback = fallbackImages.get(productId);

                if (fallback != null) {
                    result.put(productId, fallback.getImageUrl());
                }
            }
        }

        return result;
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