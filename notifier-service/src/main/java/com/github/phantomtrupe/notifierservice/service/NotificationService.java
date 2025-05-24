package com.github.phantomtrupe.notifierservice.service;

import com.github.phantomtrupe.notifierservice.config.TwilioConfig;
import com.github.phantomtrupe.notifierservice.model.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final TwilioConfig twilioConfig;

    @Value("${notification.email.subject}")
    private String emailSubject;

    public NotificationService(JavaMailSender mailSender, TwilioConfig twilioConfig) {
        this.mailSender = mailSender;
        this.twilioConfig = twilioConfig;
    }

    public Mono<Void> send(NotificationRequest request) {
        return sendEmail(request)
                .onErrorResume(e -> {
                    log.error("Email send failed", e);
                    return Mono.empty();
                })
                .then(sendSms(request)
                        .onErrorResume(e -> {
                            log.error("SMS send failed", e);
                            return Mono.empty();
                        })
                );
    }

    private Mono<Void> sendEmail(NotificationRequest request) {
        return Mono.fromRunnable(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getEmail());
            message.setSubject(emailSubject);
            message.setText(request.getMessage());
            mailSender.send(message);
            log.info("Email sent to {}", request.getEmail());
        });
    }

    private Mono<Void> sendSms(NotificationRequest request) {
        return Mono.fromCallable(() -> Message.creator(
                    new PhoneNumber(request.getPhoneNumber()),
                    new PhoneNumber(twilioConfig.getFromNumber()),
                    request.getMessage()
                ).create()
        )
        .doOnNext(msg -> log.info("SMS sent to {}: SID={}", request.getPhoneNumber(), msg.getSid()))
        .retry(2)
        .then();
    }
}
