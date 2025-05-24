package com.github.phantomtrupe.notifierservice.service;

import com.github.phantomtrupe.notifierservice.config.TwilioConfig;
import com.github.phantomtrupe.notifierservice.model.NotificationRequest;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private TwilioConfig twilioConfig;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequest request;

    @BeforeEach
    void setUp() {
        request = new NotificationRequest();
        request.setEmail("user@example.com");
        request.setPhoneNumber("+15551234567");
        request.setMessage("Test message");
        when(twilioConfig.getFromNumber()).thenReturn("+1234567890");
    }

    @Test
    void shouldSendEmailAndSms() {
        // mock Twilio Message.creator static
        MessageCreator creatorMock = mock(MessageCreator.class);
        Message messageMock = mock(Message.class);
        when(messageMock.getSid()).thenReturn("SM123");
        when(creatorMock.create()).thenReturn(messageMock);

        try (MockedStatic<Message> messageStatic = Mockito.mockStatic(Message.class)) {
            messageStatic.when(() -> Message.creator(
                    new PhoneNumber(request.getPhoneNumber()),
                    new PhoneNumber(twilioConfig.getFromNumber()),
                    request.getMessage()
            )).thenReturn(creatorMock);

            // execute
            Mono<Void> result = notificationService.send(request);

            StepVerifier.create(result)
                    .verifyComplete();

            // verify email
            verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
            // verify SMS send called
            verify(creatorMock, times(1)).create();
        }
    }

    @Test
    void shouldContinueOnEmailFailure() {
        // simulate email failure
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));
        // set up SMS success
        MessageCreator creatorMock = mock(MessageCreator.class);
        Message msg = mock(Message.class);
        when(msg.getSid()).thenReturn("SM456");
        when(creatorMock.create()).thenReturn(msg);
        try (MockedStatic<Message> msgStatic = Mockito.mockStatic(Message.class)) {
            msgStatic.when(() -> Message.creator(
                new PhoneNumber(request.getPhoneNumber()),
                new PhoneNumber(twilioConfig.getFromNumber()),
                request.getMessage()
            )).thenReturn(creatorMock);

            Mono<Void> result = notificationService.send(request);

            StepVerifier.create(result)
                .verifyComplete();

            // even if email fails, SMS is attempted
            verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
            verify(creatorMock, times(1)).create();
        }
    }

    @Test
    void shouldRetrySmsOnFailureAndSucceed() {
        // simulate message.create throwing once then succeeding
        MessageCreator creatorMock = mock(MessageCreator.class);
        Message msg = mock(Message.class);
        when(msg.getSid()).thenReturn("SM789");
        when(creatorMock.create())
            .thenThrow(new RuntimeException("twilio fail"))
            .thenReturn(msg);
        try (MockedStatic<Message> msgStatic = Mockito.mockStatic(Message.class)) {
            msgStatic.when(() -> Message.creator(
                new PhoneNumber(request.getPhoneNumber()),
                new PhoneNumber(twilioConfig.getFromNumber()),
                request.getMessage()
            )).thenReturn(creatorMock);

            Mono<Void> result = notificationService.send(request);

            StepVerifier.create(result)
                .verifyComplete();

            // email sent once
            verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
            // SMS create called twice: one retry
            verify(creatorMock, times(2)).create();
        }
    }

    @Test
    void shouldCompleteEvenIfSmsAlwaysFails() {
        // simulate SMS always failing
        MessageCreator creatorMock = mock(MessageCreator.class);
        when(creatorMock.create()).thenThrow(new RuntimeException("sms down"));
        try (MockedStatic<Message> msgStatic = Mockito.mockStatic(Message.class)) {
            msgStatic.when(() -> Message.creator(
                new PhoneNumber(request.getPhoneNumber()),
                new PhoneNumber(twilioConfig.getFromNumber()),
                request.getMessage()
            )).thenReturn(creatorMock);

            Mono<Void> result = notificationService.send(request);

            StepVerifier.create(result)
                .verifyComplete();

            // email still sent
            verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
            // SMS attempted 3 times: initial + 2 retries
            verify(creatorMock, times(3)).create();
        }
    }
}
