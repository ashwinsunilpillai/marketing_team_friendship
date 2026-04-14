package com.marketing.m2.crm;

import com.marketing.entity.Customer;
import com.marketing.m2.exceptions.CRMConnectionException;
import com.marketing.m2.exceptions.CustomerNotFoundException;

public interface ICRMConnector {
    Customer getCustomerById(String id) throws CRMConnectionException, CustomerNotFoundException;
}
