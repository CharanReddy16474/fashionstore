package com.fashionstore.dao;

import com.fashionstore.model.Brand;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface BrandDAO {

    long createBrand(Brand brand) throws SQLException;

    Optional<Brand> findById(int brandId) throws SQLException;

    List<Brand> findAllActive() throws SQLException;

    boolean updateBrand(Brand brand) throws SQLException;

    boolean updateActiveStatus(int brandId, boolean active)
            throws SQLException;

    boolean deleteBrand(int brandId) throws SQLException;
}