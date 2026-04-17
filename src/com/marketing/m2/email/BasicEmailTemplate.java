package com.marketing.m2.email;

import com.marketing.m2.exceptions.InvalidEmailTemplateException;

public class BasicEmailTemplate extends EmailTemplate {

    private final String message;

    public BasicEmailTemplate(String message) {
        this.message = message;
    }

    @Override
    protected void validate() throws InvalidEmailTemplateException {
        if (message == null || message.trim().isEmpty()) {
            throw new InvalidEmailTemplateException("Email template message cannot be null or empty.");
        }
    }

    @Override
    protected String buildContent() {
        return "[Basic Email] " + message.trim();
    }
}
