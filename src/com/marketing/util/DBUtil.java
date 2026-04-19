package com.marketing.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBUtil - Singleton pattern
 * Manages database connection pool for the Marketing ERP subsystem.
 * Follows the Singleton creational pattern to ensure one shared connection
 * source.
 * GRASP: Information Expert (knows how to manage DB connections)
 */
public class DBUtil {
    private static DBUtil instance;
    private Connection connection;

    // Database configuration - adjust these based on your setup
    private static final String DB_URL = "jdbc:mysql://localhost:3306/marketing_erp";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "p123";
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Private constructor to prevent instantiation
     */
    private DBUtil() {
        try {
            Class.forName(JDBC_DRIVER);
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connection established successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Returns the singleton instance of DBUtil
     * 
     * @return DBUtil singleton instance
     */
    public static synchronized DBUtil getInstance() {
        if (instance == null) {
            instance = new DBUtil();
        }
        return instance;
    }

    /**
     * Gets the database connection
     * 
     * @return Connection object
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving connection: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Closes the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
