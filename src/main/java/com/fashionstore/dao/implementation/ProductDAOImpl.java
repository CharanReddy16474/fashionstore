package com.fashionstore.dao.implementation;

import com.fashionstore.dao.ProductDAO;
import com.fashionstore.model.Product;
import com.fashionstore.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAOImpl implements ProductDAO {

    @Override
    public long createProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products "
                + "(product_name, description, category_id, brand_id, "
                + "price, discount_price, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, product.getProductName());
            statement.setString(2, product.getDescription());
            statement.setInt(3, product.getCategoryId());

            if (product.getBrandId() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, product.getBrandId());
            }

            statement.setBigDecimal(5, product.getPrice());
            statement.setBigDecimal(6, product.getDiscountPrice());
            statement.setBoolean(7, product.isActive());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating product failed: no generated ID returned.");
    }

    @Override
    public Optional<Product> findById(long productId) throws SQLException {
        return findOne(
                "SELECT * FROM products WHERE product_id = ?",
                productId);
    }

    @Override
    public Optional<Product> findActiveById(long productId)
            throws SQLException {

        return findOne(
                "SELECT * FROM products "
                + "WHERE product_id = ? AND is_active = TRUE",
                productId);
    }

    @Override
    public List<Product> findAllActive() throws SQLException {
        String sql = "SELECT * FROM products "
                + "WHERE is_active = TRUE ORDER BY created_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return mapProducts(resultSet);
        }
    }

    @Override
    public List<Product> findActiveByCategoryId(int categoryId)
            throws SQLException {

        String sql = "SELECT * FROM products "
                + "WHERE category_id = ? AND is_active = TRUE "
                + "ORDER BY created_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapProducts(resultSet);
            }
        }
    }

    @Override
    public List<Product> findActiveByBrandId(int brandId)
            throws SQLException {

        String sql = "SELECT * FROM products "
                + "WHERE brand_id = ? AND is_active = TRUE "
                + "ORDER BY created_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, brandId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapProducts(resultSet);
            }
        }
    }

    @Override
    public List<Product> searchActive(String keyword) throws SQLException {
        return searchActiveProducts(
                keyword, null, null, null, null,
                null, null, "newest", 0, Integer.MAX_VALUE);
    }

    @Override
    public List<Product> searchActiveProducts(
            String keyword,
            Integer categoryId,
            Integer brandId,
            String size,
            String color,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            int offset,
            int limit) throws SQLException {

        if (offset < 0 || limit < 1) {
            throw new IllegalArgumentException(
                    "Offset must be >= 0 and limit must be > 0.");
        }

        QueryParts parts = buildFilters(
                keyword, categoryId, brandId, size, color,
                minPrice, maxPrice);

        String sql = "SELECT DISTINCT p.* FROM products p "
                + "LEFT JOIN brands b ON b.brand_id = p.brand_id "
                + "LEFT JOIN product_variants pv "
                + "ON pv.product_id = p.product_id "
                + parts.whereClause
                + " ORDER BY " + safeSort(sortBy)
                + " LIMIT ? OFFSET ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int index = bindParameters(statement, parts.parameters);

            statement.setInt(index++, limit);
            statement.setInt(index, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapProducts(resultSet);
            }
        }
    }

    @Override
    public long countActiveProducts(
            String keyword,
            Integer categoryId,
            Integer brandId,
            String size,
            String color,
            BigDecimal minPrice,
            BigDecimal maxPrice) throws SQLException {

        QueryParts parts = buildFilters(
                keyword, categoryId, brandId, size, color,
                minPrice, maxPrice);

        String sql = "SELECT COUNT(DISTINCT p.product_id) "
                + "FROM products p "
                + "LEFT JOIN brands b ON b.brand_id = p.brand_id "
                + "LEFT JOIN product_variants pv "
                + "ON pv.product_id = p.product_id "
                + parts.whereClause;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            bindParameters(statement, parts.parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    @Override
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET "
                + "product_name = ?, description = ?, category_id = ?, "
                + "brand_id = ?, price = ?, discount_price = ?, "
                + "is_active = ? WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, product.getProductName());
            statement.setString(2, product.getDescription());
            statement.setInt(3, product.getCategoryId());

            if (product.getBrandId() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, product.getBrandId());
            }

            statement.setBigDecimal(5, product.getPrice());
            statement.setBigDecimal(6, product.getDiscountPrice());
            statement.setBoolean(7, product.isActive());
            statement.setLong(8, product.getProductId());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateActiveStatus(long productId, boolean active)
            throws SQLException {

        String sql = "UPDATE products "
                + "SET is_active = ? WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, active);
            statement.setLong(2, productId);

            return statement.executeUpdate() > 0;
        }
    }

    private Optional<Product> findOne(String sql, long productId)
            throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapProduct(resultSet))
                        : Optional.empty();
            }
        }
    }

    private List<Product> mapProducts(ResultSet resultSet)
            throws SQLException {

        List<Product> products = new ArrayList<>();

        while (resultSet.next()) {
            products.add(mapProduct(resultSet));
        }

        return products;
    }

    private Product mapProduct(ResultSet resultSet) throws SQLException {
        Product product = new Product();

        product.setProductId(resultSet.getLong("product_id"));
        product.setProductName(resultSet.getString("product_name"));
        product.setDescription(resultSet.getString("description"));
        product.setCategoryId(resultSet.getInt("category_id"));
        product.setBrandId(
                resultSet.getObject("brand_id", Integer.class));
        product.setPrice(resultSet.getBigDecimal("price"));
        product.setDiscountPrice(
                resultSet.getBigDecimal("discount_price"));
        product.setActive(resultSet.getBoolean("is_active"));
        product.setCreatedAt(resultSet.getTimestamp("created_at"));
        product.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return product;
    }

    private QueryParts buildFilters(
            String keyword,
            Integer categoryId,
            Integer brandId,
            String size,
            String color,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        StringBuilder where =
                new StringBuilder(" WHERE p.is_active = TRUE");

        List<Object> parameters = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            where.append(
                    " AND (p.product_name LIKE ? "
                    + "OR p.description LIKE ? "
                    + "OR b.brand_name LIKE ?)");

            String pattern = "%" + keyword.trim() + "%";

            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }

        if (categoryId != null) {
            where.append(" AND p.category_id = ?");
            parameters.add(categoryId);
        }

        if (brandId != null) {
            where.append(" AND p.brand_id = ?");
            parameters.add(brandId);
        }

        if (size != null && !size.isBlank()) {
            where.append(" AND pv.size = ?");
            parameters.add(size.trim());
        }

        if (color != null && !color.isBlank()) {
            where.append(" AND pv.color = ?");
            parameters.add(color.trim());
        }

        if (minPrice != null) {
            where.append(
                    " AND COALESCE(p.discount_price, p.price) >= ?");
            parameters.add(minPrice);
        }

        if (maxPrice != null) {
            where.append(
                    " AND COALESCE(p.discount_price, p.price) <= ?");
            parameters.add(maxPrice);
        }

        return new QueryParts(where.toString(), parameters);
    }

    private int bindParameters(
            PreparedStatement statement,
            List<Object> parameters) throws SQLException {

        int index = 1;

        for (Object parameter : parameters) {
            statement.setObject(index++, parameter);
        }

        return index;
    }

    private String safeSort(String sortBy) {
        if (sortBy == null) {
            return "p.created_at DESC";
        }

        return switch (sortBy) {
            case "price_asc" ->
                    "COALESCE(p.discount_price, p.price) ASC, p.product_id ASC";
            case "price_desc" ->
                    "COALESCE(p.discount_price, p.price) DESC, p.product_id ASC";
            case "name_asc" -> "p.product_name ASC";
            case "name_desc" -> "p.product_name DESC";
            case "newest" -> "p.created_at DESC, p.product_id DESC";
            default -> "p.created_at DESC, p.product_id DESC";
        };
    }

    private static class QueryParts {
        private final String whereClause;
        private final List<Object> parameters;

        private QueryParts(
                String whereClause, List<Object> parameters) {
            this.whereClause = whereClause;
            this.parameters = parameters;
        }
    }
}