package com.marketing.test;

import com.marketing.facade.EmailTemplateFacade;
import com.marketing.facade.LeadFacade;
import com.marketing.entity.EmailTemplateRecord;
import com.marketing.entity.EmailRecord;
import com.marketing.entity.Lead;
import com.marketing.m2.email.BasicEmailTemplate;
import com.marketing.m2.email.EmailService;

public class TestFlows {
    public static void main(String[] args) {
        EmailTemplateFacade tmplFacade = new EmailTemplateFacade();
        LeadFacade leadFacade = new LeadFacade();
        EmailService emailService = new EmailService();

        System.out.println("Starting headless test flows...");

        // Create template
        EmailTemplateRecord tmpl = new EmailTemplateRecord();
        tmpl.setName("AutoTest Template");
        tmpl.setSubject("AutoTest Subject");
        tmpl.setBody("Hello from automated test.");
        boolean created = tmplFacade.createTemplate(tmpl);
        System.out.println("Template created: " + created + " id=" + tmpl.getTemplateId());

        // Create lead
        Lead lead = new Lead();
        lead.setName("Test Lead");
        lead.setEmail("test+autotest@example.com");
        lead.setCampaignId(0);
        lead.setState("New");
        boolean leadCreated = leadFacade.createLead(lead);
        System.out.println("Lead created: " + leadCreated + " id=" + lead.getLeadId());

        // Create email record
        EmailRecord er = new EmailRecord();
        er.setTemplateId(tmpl.getTemplateId());
        er.setRecipient(lead.getEmail());
        er.setSubject(tmpl.getSubject());
        er.setBody(tmpl.getBody());
        er.setStatus("PENDING");
        int emailId = tmplFacade.createEmailRecord(er);
        System.out.println("Email record created id=" + emailId);

        // Attempt send
        try {
            emailService.sendEmail(er.getRecipient(), new BasicEmailTemplate(er.getBody()));
            tmplFacade.updateEmailStatus(emailId, "SENT");
            System.out.println("Email sent successfully.");
        } catch (Exception e) {
            tmplFacade.updateEmailStatus(emailId, "FAILED");
            System.out.println("Email send failed: " + e.getMessage());
        }

        System.out.println("Test flows completed.");
    }
}
