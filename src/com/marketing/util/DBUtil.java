package com.marketing.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

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
    private static final String DB_PASSWORD = "ROOTPASSWORD";
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Private constructor to prevent instantiation
     */
    private DBUtil() {
        try {
            Class.forName(JDBC_DRIVER);
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connection established successfully.");
            // Ensure required schema elements exist for extended marketing features
            ensureSchema();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Create missing tables/columns required by the marketing subsystem extensions.
     */
    private void ensureSchema() {
        try {
            java.sql.DatabaseMetaData meta = connection.getMetaData();

            // Ensure campaigns has lead_target and leads_generated columns
            try (ResultSet cols = meta.getColumns(null, null, "campaigns", "lead_target")) {
                if (!cols.next()) {
                    try (Statement s = connection.createStatement()) {
                        s.executeUpdate("ALTER TABLE campaigns ADD COLUMN lead_target INT DEFAULT 100");
                        System.out.println("Added column lead_target to campaigns");
                    } catch (SQLException ex) {
                        System.err.println("Could not add lead_target column: " + ex.getMessage());
                    }
                }
            }

            try (ResultSet cols = meta.getColumns(null, null, "campaigns", "leads_generated")) {
                if (!cols.next()) {
                    try (Statement s = connection.createStatement()) {
                        s.executeUpdate("ALTER TABLE campaigns ADD COLUMN leads_generated INT DEFAULT 0");
                        System.out.println("Added column leads_generated to campaigns");
                    } catch (SQLException ex) {
                        System.err.println("Could not add leads_generated column: " + ex.getMessage());
                    }
                }
            }

            // Ensure campaigns has campaign_type column
            try (ResultSet cols = meta.getColumns(null, null, "campaigns", "campaign_type")) {
                if (!cols.next()) {
                    try (Statement s = connection.createStatement()) {
                        s.executeUpdate("ALTER TABLE campaigns ADD COLUMN campaign_type VARCHAR(50) DEFAULT 'EMAIL'");
                        System.out.println("Added column campaign_type to campaigns");
                    } catch (SQLException ex) {
                        System.err.println("Could not add campaign_type column: " + ex.getMessage());
                    }
                }
            }

            // Create leads table if missing
            try (ResultSet tables = meta.getTables(null, null, "leads", new String[] {"TABLE"})) {
                if (!tables.next()) {
                    try (Statement s = connection.createStatement()) {
                        s.executeUpdate("CREATE TABLE IF NOT EXISTS leads (lead_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255), campaign_id INT, state VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");
                        System.out.println("Created table leads");
                    } catch (SQLException ex) {
                        System.err.println("Could not create leads table: " + ex.getMessage());
                    }
                }
            }

            // Create email_templates table if missing
            try (ResultSet tables = meta.getTables(null, null, "email_templates", new String[] {"TABLE"})) {
                if (!tables.next()) {
                    try (Statement s = connection.createStatement()) {
                        s.executeUpdate("CREATE TABLE IF NOT EXISTS email_templates (template_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), subject VARCHAR(255), body TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");
                        System.out.println("Created table email_templates");
                    } catch (SQLException ex) {
                        System.err.println("Could not create email_templates table: " + ex.getMessage());
                    }
                }
            }

            // Create emails table if missing
            try (ResultSet tables = meta.getTables(null, null, "emails", new String[] {"TABLE"})) {
                if (!tables.next()) {
                    try (Statement s = connection.createStatement()) {
                        s.executeUpdate("CREATE TABLE IF NOT EXISTS emails (email_id INT AUTO_INCREMENT PRIMARY KEY, template_id INT, recipient VARCHAR(255), subject VARCHAR(255), body TEXT, status VARCHAR(50) DEFAULT 'PENDING', sent_at TIMESTAMP NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                        System.out.println("Created table emails");
                    } catch (SQLException ex) {
                        System.err.println("Could not create emails table: " + ex.getMessage());
                    }
                }
            }

            // Create crm_sync_log table if missing
            try (ResultSet tables = meta.getTables(null, null, "crm_sync_log", new String[] {"TABLE"})) {
                if (!tables.next()) {
                    try (Statement s = connection.createStatement()) {
                        s.executeUpdate("CREATE TABLE IF NOT EXISTS crm_sync_log (log_id INT AUTO_INCREMENT PRIMARY KEY, source VARCHAR(100), details TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                        System.out.println("Created table crm_sync_log");
                    } catch (SQLException ex) {
                        System.err.println("Could not create crm_sync_log table: " + ex.getMessage());
                    }
                }
            }

            // Final safety: ensure leads table exists (attempt unconditional IF NOT EXISTS to cover edge cases)
            try {
                try (Statement s = connection.createStatement()) {
                    s.executeUpdate("CREATE TABLE IF NOT EXISTS leads (lead_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255), campaign_id INT, state VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");
                    System.out.println("Ensured table leads exists (safety check)");
                }
            } catch (SQLException ex) {
                System.err.println("Final leads table ensure failed: " + ex.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Schema check failed: " + e.getMessage());
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
            // Return a fresh connection for each caller to avoid sharing the singleton
            // Connection across threads and risking ResultSet/Statement lifecycle issues.
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error retrieving connection: " + e.getMessage());
            return null;
        }
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
