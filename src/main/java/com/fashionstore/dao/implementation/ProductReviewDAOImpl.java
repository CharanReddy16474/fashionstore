package com.fashionstore.dao.implementation;

import com.fashionstore.dao.ProductReviewDAO;
import com.fashionstore.model.ProductReview;
import com.fashionstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductReviewDAOImpl implements ProductReviewDAO {

    @Override
    public long createReview(ProductReview review) throws SQLException {
        validateRating(review.getRating());

        String sql = "INSERT INTO product_reviews "
                + "(user_id, product_id, rating, review_text) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, review.getUserId());
            statement.setLong(2, review.getProductId());
            statement.setInt(3, review.getRating());
            statement.setString(4, review.getReviewText());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating product review failed: no generated ID returned.");
    }

    @Override
    public Optional<ProductReview> findById(long reviewId)
            throws SQLException {

        String sql = "SELECT * FROM product_reviews WHERE review_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, reviewId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapReview(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public List<ProductReview> findByProductId(
            long productId, int offset, int limit)
            throws SQLException {

        validatePage(offset, limit);

        String sql = "SELECT * FROM product_reviews "
                + "WHERE product_id = ? "
                + "ORDER BY created_at DESC LIMIT ? OFFSET ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<ProductReview> reviews = new ArrayList<>();

                while (resultSet.next()) {
                    reviews.add(mapReview(resultSet));
                }

                return reviews;
            }
        }
    }

    @Override
    public List<ProductReview> findByUserId(long userId)
            throws SQLException {

        String sql = "SELECT * FROM product_reviews "
                + "WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<ProductReview> reviews = new ArrayList<>();

                while (resultSet.next()) {
                    reviews.add(mapReview(resultSet));
                }

                return reviews;
            }
        }
    }

    @Override
    public Optional<ProductReview> findByUserAndProduct(
            long userId, long productId) throws SQLException {

        String sql = "SELECT * FROM product_reviews "
                + "WHERE user_id = ? AND product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setLong(2, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapReview(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public double getAverageRating(long productId) throws SQLException {
        String sql = "SELECT COALESCE(AVG(rating), 0) AS average_rating "
                + "FROM product_reviews WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getDouble("average_rating");
            }
        }
    }

    @Override
    public long countByProductId(long productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product_reviews "
                + "WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    @Override
    public boolean updateReview(ProductReview review) throws SQLException {
        validateRating(review.getRating());

        String sql = "UPDATE product_reviews "
                + "SET rating = ?, review_text = ? "
                + "WHERE review_id = ? AND user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, review.getRating());
            statement.setString(2, review.getReviewText());
            statement.setLong(3, review.getReviewId());
            statement.setLong(4, review.getUserId());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteReview(long reviewId) throws SQLException {
        String sql = "DELETE FROM product_reviews WHERE review_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, reviewId);

            return statement.executeUpdate() > 0;
        }
    }

    private ProductReview mapReview(ResultSet resultSet)
            throws SQLException {

        ProductReview review = new ProductReview();

        review.setReviewId(resultSet.getLong("review_id"));
        review.setUserId(resultSet.getLong("user_id"));
        review.setProductId(resultSet.getLong("product_id"));
        review.setRating(resultSet.getInt("rating"));
        review.setReviewText(resultSet.getString("review_text"));
        review.setCreatedAt(resultSet.getTimestamp("created_at"));

        return review;
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5.");
        }
    }

    private void validatePage(int offset, int limit) {
        if (offset < 0 || limit < 1) {
            throw new IllegalArgumentException(
                    "Offset must be >= 0 and limit must be > 0.");
        }
    }
}