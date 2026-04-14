package com.marketing.m2.crm;

import com.marketing.entity.Customer;

public class ExternalCRMSystem {

    public Customer fetchCustomer(String id) {
        double randomValue = Math.random();

        if (randomValue < 0.30) {
            throw new RuntimeException("External CRM is unavailable.");
        }

        if (randomValue < 0.60) {
            return null;
        }

        int customerId;
        try {
            customerId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            customerId = 0;
        }

        return new Customer(
                customerId,
                "John",
                "Doe",
                "john.doe@example.com",
                "123-456-7890",
                "Colombo",
                30,
                "Technology",
                "ACTIVE"
        );
    }
}
