package com.fashionstore.dao;

import com.fashionstore.model.ProductReview;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductReviewDAO {

    long createReview(ProductReview review) throws SQLException;

    Optional<ProductReview> findById(long reviewId)
            throws SQLException;

    List<ProductReview> findByProductId(
            long productId, int offset, int limit)
            throws SQLException;

    List<ProductReview> findByUserId(long userId)
            throws SQLException;

    Optional<ProductReview> findByUserAndProduct(
            long userId, long productId)
            throws SQLException;

    double getAverageRating(long productId) throws SQLException;

    long countByProductId(long productId) throws SQLException;

    boolean updateReview(ProductReview review) throws SQLException;

    boolean deleteReview(long reviewId) throws SQLException;
}