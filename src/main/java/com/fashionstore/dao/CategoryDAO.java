package com.fashionstore.dao;

import com.fashionstore.model.Category;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CategoryDAO {

    long createCategory(Category category) throws SQLException;

    Optional<Category> findById(int categoryId) throws SQLException;

    Optional<Category> findBySlug(String categorySlug)
            throws SQLException;

    List<Category> findAllActive() throws SQLException;

    List<Category> findTopLevelActive() throws SQLException;

    List<Category> findActiveChildren(int parentCategoryId)
            throws SQLException;

    boolean updateCategory(Category category) throws SQLException;

    boolean updateActiveStatus(int categoryId, boolean active)
            throws SQLException;

    boolean deleteCategory(int categoryId) throws SQLException;
}