package com.fashionstore.dao;

import com.fashionstore.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductDAO {

    long createProduct(Product product) throws SQLException;

    Optional<Product> findById(long productId) throws SQLException;

    Optional<Product> findActiveById(long productId)
            throws SQLException;

    List<Product> findAllActive() throws SQLException;

    List<Product> findActiveByCategoryId(int categoryId)
            throws SQLException;

    List<Product> findActiveByBrandId(int brandId)
            throws SQLException;

    List<Product> searchActive(String keyword) throws SQLException;

    List<Product> searchActiveProducts(
            String keyword,
            Integer categoryId,
            Integer brandId,
            String size,
            String color,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            int offset,
            int limit
    ) throws SQLException;

    long countActiveProducts(
            String keyword,
            Integer categoryId,
            Integer brandId,
            String size,
            String color,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) throws SQLException;

    boolean updateProduct(Product product) throws SQLException;

    boolean updateActiveStatus(long productId, boolean active)
            throws SQLException;
}