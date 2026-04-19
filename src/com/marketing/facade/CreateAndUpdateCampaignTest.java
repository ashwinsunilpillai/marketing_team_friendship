package com.marketing.facade;

import com.marketing.entity.Campaign;

public class CreateAndUpdateCampaignTest {
    public static void main(String[] args) {
        CampaignFacade facade = new CampaignFacade();
        Campaign c = new Campaign();
        c.setCampaignName("Smoke Test Campaign Update " + System.currentTimeMillis());
        c.setStartDate(java.time.LocalDate.now());
        c.setEndDate(java.time.LocalDate.now().plusDays(10));
        c.setBudget(5000.00);
        c.setStatus("PLANNED");
        c.setSegmentId(1);
        c.setDescription("Created for update test");
        c.setLeadTarget(75);
        c.setLeadsGenerated(0);
        c.setCampaignType("EMAIL");

        try {
            boolean ok = facade.createCampaign(c);
            System.out.println("createCampaign returned: " + ok + ", id=" + c.getCampaignId());

            // Modify fields and update
            c.setCampaignName(c.getCampaignName() + " (updated)");
            c.setBudget(7500.00);
            c.setLeadTarget(120);

            boolean updated = facade.updateCampaign(c);
            System.out.println("updateCampaign returned: " + updated + ", id=" + c.getCampaignId());

            // Fetch back
            Campaign fetched = facade.getCampaignById(c.getCampaignId());
            System.out.println("Fetched campaign: " + fetched);

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
