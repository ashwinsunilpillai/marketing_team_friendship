package com.marketing.m2.lead;

public class NewLeadState implements LeadState {

    @Override
    public void handle(Lead lead) {
        System.out.println("Lead '" + lead.getName() + "' transitioned from New to Qualified.");
        lead.setState(new QualifiedLeadState());
    }

    @Override
    public String getStateName() {
        return "New";
    }
}
