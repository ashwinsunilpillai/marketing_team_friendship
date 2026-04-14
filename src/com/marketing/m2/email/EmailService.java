package com.marketing.m2.email;

import com.marketing.m2.exceptions.EmailSendException;
import com.marketing.m2.exceptions.InvalidEmailTemplateException;

public class EmailService {

    private static final int MAX_ATTEMPTS = 3;

    public void sendEmail(String recipient, EmailTemplate template)
            throws InvalidEmailTemplateException, EmailSendException {
        int attempt = 0;

        while (attempt < MAX_ATTEMPTS) {
            attempt++;
            try {
                boolean simulatedFailure = Math.random() < 0.5;
                if (simulatedFailure) {
                    throw new RuntimeException("Simulated email delivery failure.");
                }

                template.send(recipient);
                return;
            } catch (InvalidEmailTemplateException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("Email send attempt " + attempt + " failed. Retrying...");
                if (attempt >= MAX_ATTEMPTS) {
                    throw new EmailSendException("Failed to send email after " + MAX_ATTEMPTS + " attempts.");
                }
            }
        }
    }
}
