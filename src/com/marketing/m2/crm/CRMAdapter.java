package com.marketing.m2.crm;

import com.marketing.entity.Customer;
import com.marketing.m2.exceptions.CRMConnectionException;
import com.marketing.m2.exceptions.CustomerNotFoundException;

public class CRMAdapter implements ICRMConnector {

    private final ExternalCRMSystem externalCRMSystem;

    public CRMAdapter() {
        this.externalCRMSystem = new ExternalCRMSystem();
    }

    @Override
    public Customer getCustomerById(String id) throws CRMConnectionException, CustomerNotFoundException {
        try {
            Customer customer = externalCRMSystem.fetchCustomer(id);
            if (customer == null) {
                throw new CustomerNotFoundException("Customer data not found in CRM");
            }
            return customer;
        } catch (CustomerNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CRMConnectionException("Unable to connect to CRM system");
        }
    }
}
