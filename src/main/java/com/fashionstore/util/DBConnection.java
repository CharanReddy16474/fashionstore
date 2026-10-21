
package com.fashionstore.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String LOCAL_URL =
            "jdbc:mysql://localhost:3306/fashion_store";

    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int SOCKET_TIMEOUT_MS = 30000;

    private DBConnection() {
        // Utility class.
    }

    private static String getEnv(String key) {
        String value = System.getenv(key);
        return value == null ? null : value.trim();
    }

    private static String requiredEnv(String key)
            throws SQLException {

        String value = getEnv(key);

        if (value == null || value.isBlank()) {
            throw new SQLException(
                    "Missing required environment variable: " + key
            );
        }

        return value;
    }

    public static Connection getConnection()
            throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL JDBC driver not found. "
                    + "Check Maven Dependencies.",
                    e
            );
        }

        String host = getEnv("DB_HOST");

        String url;
        String username;
        String password;

        // Local development configuration.
        if (host == null || host.isBlank()) {

            if ("true".equalsIgnoreCase(getEnv("RENDER"))) {
                throw new SQLException(
                        "DB_HOST is missing in Render environment variables."
                );
            }

            username = getEnv("LOCAL_DB_USERNAME");
            password = getEnv("LOCAL_DB_PASSWORD");

            if (username == null || username.isBlank()) {
                username = "root";
            }

            if (password == null || password.isBlank()) {
                throw new SQLException(
                        "Local MySQL password is missing. "
                        + "Set LOCAL_DB_PASSWORD in Eclipse."
                );
            }

            url = LOCAL_URL
                    + "?connectTimeout=" + CONNECT_TIMEOUT_MS
                    + "&socketTimeout=" + SOCKET_TIMEOUT_MS
                    + "&tcpKeepAlive=true"
                    + "&cachePrepStmts=true"
                    + "&prepStmtCacheSize=250"
                    + "&prepStmtCacheSqlLimit=2048"
                    + "&useServerPrepStmts=true";

        } else {

            // Render/Aiven configuration.
            String port = requiredEnv("DB_PORT");
            String database = requiredEnv("DB_NAME");

            username = requiredEnv("DB_USERNAME");
            password = requiredEnv("DB_PASSWORD");

            url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?sslMode=REQUIRED"
                    + "&connectTimeout=" + CONNECT_TIMEOUT_MS
                    + "&socketTimeout=" + SOCKET_TIMEOUT_MS
                    + "&tcpKeepAlive=true"
                    + "&cachePrepStmts=true"
                    + "&prepStmtCacheSize=250"
                    + "&prepStmtCacheSqlLimit=2048"
                    + "&useServerPrepStmts=true";
        }

        return DriverManager.getConnection(url, username, password);
    }

    public static void shutdown() {
        // No shared connection pool to close.
    }

    // Optional database connection test.
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
