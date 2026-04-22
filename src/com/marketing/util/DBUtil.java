package com.marketing.util;

import com.likeseca.erp.database.facade.ErpDatabaseFacade;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
    private static DBUtil instance;
    private ErpDatabaseFacade erpDatabaseFacade;

    private DBUtil() {
        try {
            this.erpDatabaseFacade = new ErpDatabaseFacade();
            System.out.println("ERP Database Facade initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize ERP Database Facade: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized DBUtil getInstance() {
        if (instance == null) {
            instance = new DBUtil();
        }
        return instance;
    }

    public ErpDatabaseFacade getErpDatabaseFacade() {
        return erpDatabaseFacade;
    }

    public Object getMarketingSubsystem() {
        return erpDatabaseFacade != null ? erpDatabaseFacade.marketingSubsystem() : null;
    }

    // Backward-compatible JDBC accessor for legacy code paths still using Connection.
    public Connection getConnection() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("database.properties")) {
            props.load(fis);
            String host = props.getProperty("db.host", "127.0.0.1");
            String port = props.getProperty("db.port", "3306");
            String dbName = props.getProperty("db.name", "erp_subsystem");
            String user = props.getProperty("db.username", "root");
            String password = props.getProperty("db.password", "");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
            return DriverManager.getConnection(url, user, password);
        } catch (IOException | SQLException e) {
            System.err.println("Error creating JDBC connection: " + e.getMessage());
            return null;
        }
    }

    public void closeConnection() {
        try {
            if (erpDatabaseFacade != null) {
                erpDatabaseFacade.close();
                System.out.println("ERP Database Facade connection closed.");
            }
        } catch (Exception e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
