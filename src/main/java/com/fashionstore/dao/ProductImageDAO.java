package com.fashionstore.dao;

import com.fashionstore.model.ProductImage;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductImageDAO {

    long createImage(ProductImage image) throws SQLException;

    Optional<ProductImage> findById(long imageId) throws SQLException;

    List<ProductImage> findByProductId(long productId)
            throws SQLException;

    Optional<ProductImage> findPrimaryByProductId(long productId)
            throws SQLException;

    boolean updateImage(ProductImage image) throws SQLException;

    boolean setPrimaryImage(long productId, long imageId)
            throws SQLException;

    boolean deleteImage(long imageId) throws SQLException;
}