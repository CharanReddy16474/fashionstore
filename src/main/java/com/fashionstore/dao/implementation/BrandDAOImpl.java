package com.fashionstore.dao.implementation;

import com.fashionstore.dao.BrandDAO;
import com.fashionstore.model.Brand;
import com.fashionstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BrandDAOImpl implements BrandDAO {

    @Override
    public long createBrand(Brand brand) throws SQLException {
        String sql = "INSERT INTO brands (brand_name, is_active) "
                + "VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, brand.getBrandName());
            statement.setBoolean(2, brand.isActive());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating brand failed: no generated ID returned.");
    }

    @Override
    public Optional<Brand> findById(int brandId) throws SQLException {
        String sql = "SELECT * FROM brands WHERE brand_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, brandId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapBrand(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public List<Brand> findAllActive() throws SQLException {
        String sql = "SELECT * FROM brands "
                + "WHERE is_active = TRUE ORDER BY brand_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<Brand> brands = new ArrayList<>();

            while (resultSet.next()) {
                brands.add(mapBrand(resultSet));
            }

            return brands;
        }
    }

    @Override
    public boolean updateBrand(Brand brand) throws SQLException {
        String sql = "UPDATE brands "
                + "SET brand_name = ?, is_active = ? WHERE brand_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, brand.getBrandName());
            statement.setBoolean(2, brand.isActive());
            statement.setInt(3, brand.getBrandId());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateActiveStatus(int brandId, boolean active)
            throws SQLException {

        String sql = "UPDATE brands "
                + "SET is_active = ? WHERE brand_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, active);
            statement.setInt(2, brandId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteBrand(int brandId) throws SQLException {
        String sql = "DELETE FROM brands WHERE brand_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, brandId);

            return statement.executeUpdate() > 0;
        }
    }

    private Brand mapBrand(ResultSet resultSet) throws SQLException {
        Brand brand = new Brand();

        brand.setBrandId(resultSet.getInt("brand_id"));
        brand.setBrandName(resultSet.getString("brand_name"));
        brand.setActive(resultSet.getBoolean("is_active"));
        brand.setCreatedAt(resultSet.getTimestamp("created_at"));

        return brand;
    }
}