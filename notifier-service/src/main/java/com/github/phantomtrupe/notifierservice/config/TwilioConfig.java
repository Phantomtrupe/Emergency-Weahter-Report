package com.github.phantomtrupe.notifierservice.config;

import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class TwilioConfig {
    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    public String getFromNumber() {
        return fromNumber;
    }

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }
}
