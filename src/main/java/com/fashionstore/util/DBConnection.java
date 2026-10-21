
package com.fashionstore.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String LOCAL_URL =
            "jdbc:mysql://localhost:3306/fashion_store";

    private static String getEnv(String key) {
        String value = System.getenv(key);
        return value == null ? null : value.trim();
    }

    private static String requiredEnv(String key) throws SQLException {
        String value = getEnv(key);

        if (value == null || value.isBlank()) {
            throw new SQLException(
                    "Missing required environment variable: " + key
            );
        }

        return value;
    }

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL JDBC driver not found. "
                    + "Check the Maven dependency.",
                    e
            );
        }

        String host = getEnv("DB_HOST");
        String url;
        String username;
        String password;

        // Local development: preserve the existing localhost database.
        if (host == null || host.isBlank()) {

            // Render sets RENDER=true. Never silently fall back to localhost
            // when production database configuration is missing.
            if ("true".equalsIgnoreCase(getEnv("RENDER"))) {
                throw new SQLException(
                        "DB_HOST is missing in Render environment variables."
                );
            }

            url = LOCAL_URL;

            username = getEnv("LOCAL_DB_USERNAME");
            if (username == null || username.isBlank()) {
                username = "root";
            }

            password = getEnv("LOCAL_DB_PASSWORD");
            if (password == null) {
                password = "";
            }

        } else {

            // Deployed application: connect to Aiven using environment values.
            String port = requiredEnv("DB_PORT");
            String database = requiredEnv("DB_NAME");

            username = requiredEnv("DB_USERNAME");
            password = requiredEnv("DB_PASSWORD");

            url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?sslMode=REQUIRED";
        }

        return DriverManager.getConnection(url, username, password);
    }

    // Local connection test.
    public static void main(String[] args) {

        try (Connection connection = getConnection()) {

            if (connection != null && !connection.isClosed()) {
                System.out.println(
                        "Fashion Store database connected successfully!"
                );
            }

        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            System.err.println("Reason: " + e.getMessage());
        }
    }
}
