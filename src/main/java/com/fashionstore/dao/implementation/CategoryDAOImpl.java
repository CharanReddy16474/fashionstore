package com.fashionstore.dao.implementation;

import com.fashionstore.dao.CategoryDAO;
import com.fashionstore.model.Category;
import com.fashionstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDAOImpl implements CategoryDAO {

    @Override
    public long createCategory(Category category) throws SQLException {
        String sql = "INSERT INTO categories "
                + "(category_name, category_slug, parent_category_id, "
                + "description, is_active) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, category.getCategoryName());
            statement.setString(2, category.getCategorySlug());

            if (category.getParentCategoryId() == null) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(3, category.getParentCategoryId());
            }

            statement.setString(4, category.getDescription());
            statement.setBoolean(5, category.isActive());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating category failed: no generated ID returned.");
    }

    @Override
    public Optional<Category> findById(int categoryId)
            throws SQLException {

        return findOne(
                "SELECT * FROM categories WHERE category_id = ?",
                categoryId);
    }

    @Override
    public Optional<Category> findBySlug(String categorySlug)
            throws SQLException {

        String sql = "SELECT * FROM categories WHERE category_slug = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, categorySlug);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapCategory(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public List<Category> findAllActive() throws SQLException {
        String sql = "SELECT * FROM categories "
                + "WHERE is_active = TRUE ORDER BY category_name";

        return queryCategories(sql);
    }

    @Override
    public List<Category> findTopLevelActive() throws SQLException {
        String sql = "SELECT * FROM categories "
                + "WHERE is_active = TRUE "
                + "AND parent_category_id IS NULL "
                + "ORDER BY category_name";

        return queryCategories(sql);
    }

    @Override
    public List<Category> findActiveChildren(int parentCategoryId)
            throws SQLException {

        String sql = "SELECT * FROM categories "
                + "WHERE is_active = TRUE AND parent_category_id = ? "
                + "ORDER BY category_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, parentCategoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Category> categories = new ArrayList<>();

                while (resultSet.next()) {
                    categories.add(mapCategory(resultSet));
                }

                return categories;
            }
        }
    }

    @Override
    public boolean updateCategory(Category category) throws SQLException {
        String sql = "UPDATE categories SET "
                + "category_name = ?, category_slug = ?, "
                + "parent_category_id = ?, description = ?, is_active = ? "
                + "WHERE category_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category.getCategoryName());
            statement.setString(2, category.getCategorySlug());

            if (category.getParentCategoryId() == null) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(3, category.getParentCategoryId());
            }

            statement.setString(4, category.getDescription());
            statement.setBoolean(5, category.isActive());
            statement.setInt(6, category.getCategoryId());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateActiveStatus(int categoryId, boolean active)
            throws SQLException {

        String sql = "UPDATE categories "
                + "SET is_active = ? WHERE category_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, active);
            statement.setInt(2, categoryId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteCategory(int categoryId) throws SQLException {
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            return statement.executeUpdate() > 0;
        }
    }

    private Optional<Category> findOne(String sql, int categoryId)
            throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapCategory(resultSet))
                        : Optional.empty();
            }
        }
    }

    private List<Category> queryCategories(String sql)
            throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<Category> categories = new ArrayList<>();

            while (resultSet.next()) {
                categories.add(mapCategory(resultSet));
            }

            return categories;
        }
    }

    private Category mapCategory(ResultSet resultSet)
            throws SQLException {

        Category category = new Category();

        category.setCategoryId(resultSet.getInt("category_id"));
        category.setCategoryName(resultSet.getString("category_name"));
        category.setCategorySlug(resultSet.getString("category_slug"));
        category.setParentCategoryId(
                resultSet.getObject("parent_category_id", Integer.class));
        category.setDescription(resultSet.getString("description"));
        category.setActive(resultSet.getBoolean("is_active"));
        category.setCreatedAt(resultSet.getTimestamp("created_at"));

        return category;
    }
}