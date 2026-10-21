package com.fashionstore.dao;

import com.fashionstore.model.User;
import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO {

    long createUser(User user) throws SQLException;

    Optional<User> findById(long userId) throws SQLException;

    Optional<User> findByEmail(String email) throws SQLException;

    boolean existsByEmail(String email) throws SQLException;

    boolean updateProfile(User user) throws SQLException;

    boolean updateEmail(long userId, String email)
            throws SQLException;

    boolean updatePasswordHash(long userId, String passwordHash)
            throws SQLException;
}