package com.marketing.m2.email;

import com.marketing.m2.exceptions.InvalidEmailTemplateException;

public abstract class EmailTemplate {

    public final void send(String recipient) throws InvalidEmailTemplateException {
        validate();
        String content = buildContent();
        deliver(recipient, content);
    }

    protected abstract void validate() throws InvalidEmailTemplateException;

    protected abstract String buildContent();

    private void deliver(String recipient, String content) {
        System.out.println("Sending email to: " + recipient);
        System.out.println("Email content: " + content);
    }
}
