package com.marketing.m2.crm;

import com.marketing.entity.Customer;
import com.marketing.m2.exceptions.CRMConnectionException;
import com.marketing.m2.exceptions.CustomerNotFoundException;
import com.marketing.facade.CRMLogFacade;

import java.util.Optional;

public class CRMService {

    private final ICRMConnector crmConnector;
    private final CRMLogFacade logFacade = new CRMLogFacade();

    public CRMService() {
        this.crmConnector = new CRMAdapter();
    }

    public Customer getCustomer(String id) {
        int attempts = 0;
        int maxAttempts = 2;

        while (attempts < maxAttempts) {
            attempts++;
            try {
                Customer c = crmConnector.getCustomerById(id);
                try {
                    logFacade.createLog("CRMService", "Fetched customer id=" + id + " success");
                } catch (Exception ignore) {}
                return c;
            } catch (CRMConnectionException e) {
                if (attempts < maxAttempts) {
                    System.out.println("CRM connection failed. Retrying...");
                    try { logFacade.createLog("CRMService", "Connection attempt failed for id=" + id + ": " + e.getMessage()); } catch (Exception ignore) {}
                } else {
                    System.out.println("Unable to connect to CRM system. Using cached data temporarily.");
                    try { logFacade.createLog("CRMService", "Giving up connecting for id=" + id + ", returning null"); } catch (Exception ignore) {}
                    return null;
                }
            } catch (CustomerNotFoundException e) {
                System.out.println("Customer data not found in CRM. Skipping customer: " + id);
                try { logFacade.createLog("CRMService", "Customer not found: id=" + id); } catch (Exception ignore) {}
                return null;
            }
        }

        return null;
    }
}
