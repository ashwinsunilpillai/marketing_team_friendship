package com.marketing.m2.lead;

public class QualifiedLeadState implements LeadState {

    @Override
    public void handle(Lead lead) {
        System.out.println("Lead '" + lead.getName() + "' transitioned from Qualified to Converted.");
        lead.setState(new ConvertedLeadState());
    }

    @Override
    public String getStateName() {
        return "Qualified";
    }
}
