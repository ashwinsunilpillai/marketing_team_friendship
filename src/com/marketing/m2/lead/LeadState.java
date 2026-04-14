package com.marketing.m2.lead;

public interface LeadState {
    void handle(Lead lead);

    String getStateName();
}
