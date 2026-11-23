package com.ebdf.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private DBConnection() {
    }

    public static Connection getConnection() {
        Properties properties = new Properties();

        try (InputStream input = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read db.properties", e);
        }

        String url = setting("EBDF_DB_URL", properties, "db.url");
        String username = setting("EBDF_DB_USERNAME", properties, "db.username");
        String password = setting("EBDF_DB_PASSWORD", properties, "db.password");
        String driver = setting("EBDF_DB_DRIVER", properties, "db.driver");

        if (isBlank(url) || isBlank(username) || isBlank(password) || isBlank(driver)
                || "CHANGE_ME".equals(password)) {
            throw new IllegalStateException(
                    "Database configuration is incomplete. Set EBDF_DB_URL, "
                    + "EBDF_DB_USERNAME and EBDF_DB_PASSWORD or update db.properties.");
        }

        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            throw new IllegalStateException("Unable to connect to Oracle Database", e);
        }
    }

    private static String setting(String envName, Properties properties, String key) {
        String value = System.getenv(envName);
        if (value == null || value.isBlank()) {
            value = properties.getProperty(key);
        }
        return value == null ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
