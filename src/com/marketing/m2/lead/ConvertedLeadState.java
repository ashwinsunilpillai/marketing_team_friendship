package com.marketing.m2.lead;

public class ConvertedLeadState implements LeadState {

    @Override
    public void handle(Lead lead) {
        System.out.println("Lead already converted");
    }

    @Override
    public String getStateName() {
        return "Converted";
    }
}
