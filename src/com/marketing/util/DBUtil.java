package com.marketing.util;

import com.likeseca.erp.database.facade.ErpDatabaseFacade;

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

    public Object getDatabaseIntegrationSubsystem() {
        return erpDatabaseFacade != null ? erpDatabaseFacade.databaseIntegrationSubsystem() : null;
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
