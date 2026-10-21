package com.fashionstore.dao;

import com.fashionstore.model.ProductVariant;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductVariantDAO {

    long createVariant(ProductVariant variant) throws SQLException;

    Optional<ProductVariant> findById(long variantId)
            throws SQLException;

    List<ProductVariant> findByProductId(long productId)
            throws SQLException;

    List<ProductVariant> findAvailableByProductId(long productId)
            throws SQLException;

    Optional<ProductVariant> findByProductSizeAndColor(
            long productId, String size, String color)
            throws SQLException;

    boolean updateVariant(ProductVariant variant)
            throws SQLException;

    boolean updateStockQuantity(long variantId, int quantity)
            throws SQLException;

    boolean decreaseStockIfAvailable(long variantId, int quantity)
            throws SQLException;

    boolean increaseStock(long variantId, int quantity)
            throws SQLException;
}