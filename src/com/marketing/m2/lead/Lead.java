package com.marketing.m2.lead;

public class Lead {

    private final String name;
    private LeadState state;

    public Lead(String name) {
        this.name = name;
        this.state = new NewLeadState();
    }

    public void setState(LeadState state) {
        this.state = state;
    }

    public void progress() {
        state.handle(this);
    }

    public String getStateName() {
        return state.getStateName();
    }

    public String getName() {
        return name;
    }
}
