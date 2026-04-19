package com.marketing.facade;

import com.marketing.entity.Campaign;

public class CreateCampaignTest {
    public static void main(String[] args) {
        CampaignFacade facade = new CampaignFacade();
        Campaign c = new Campaign();
        c.setCampaignName("Smoke Test Campaign " + System.currentTimeMillis());
        c.setStartDate(java.time.LocalDate.now());
        c.setEndDate(java.time.LocalDate.now().plusDays(10));
        c.setBudget(1234.56);
        c.setStatus("PLANNED");
        c.setSegmentId(1);
        c.setDescription("Smoke test campaign created by CreateCampaignTest");
        c.setLeadTarget(50);
        c.setLeadsGenerated(0);
        c.setCampaignType("EMAIL");

        try {
            boolean ok = facade.createCampaign(c);
            System.out.println("createCampaign returned: " + ok + ", id=" + c.getCampaignId());
        } catch (Exception e) {
            System.err.println("createCampaign failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
