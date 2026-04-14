package com.marketing.m2.crm;

import com.marketing.entity.Customer;
import com.marketing.m2.exceptions.CRMConnectionException;
import com.marketing.m2.exceptions.CustomerNotFoundException;

public class CRMService {

    private final ICRMConnector crmConnector;

    public CRMService() {
        this.crmConnector = new CRMAdapter();
    }

    public Customer getCustomer(String id) {
        int attempts = 0;
        int maxAttempts = 2;

        while (attempts < maxAttempts) {
            attempts++;
            try {
                return crmConnector.getCustomerById(id);
            } catch (CRMConnectionException e) {
                if (attempts < maxAttempts) {
                    System.out.println("CRM connection failed. Retrying...");
                } else {
                    System.out.println("Unable to connect to CRM system. Using cached data temporarily.");
                    return null;
                }
            } catch (CustomerNotFoundException e) {
                System.out.println("Customer data not found in CRM. Skipping customer: " + id);
                return null;
            }
        }

        return null;
    }
}
