package io.github.joaovitorleal.securecapita.service.implementation;

import io.github.joaovitorleal.securecapita.exception.EmailDeliveryFailureException;
import io.github.joaovitorleal.securecapita.service.NotificationService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service("emailService")
public class EmailServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String EMAIL_SUBJECT = "SecureCapita";
    private static final String ENCODING = "UTF-8";

    private final JavaMailSender mailSender;

    @Value("spring.mail.username")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * @param to Email recipient
     * @param message body of the message
     */
    @Async
    @Override
    public void sendMessage(String to, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, ENCODING);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(EMAIL_SUBJECT);
            helper.setText(message, false);

            mailSender.send(mimeMessage);
            LOGGER.info("Sent email to: {}", to);
        } catch (Exception e) {
            LOGGER.error("Error while sending email", e);
            throw new EmailDeliveryFailureException("Error while sending email", e);
        }
    }
}
