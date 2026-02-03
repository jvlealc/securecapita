package io.github.joaovitorleal.securecapita.service;

import io.github.joaovitorleal.securecapita.exception.EmailDeliveryFailureException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service("emailService")
public class EmailService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
    private static final String GENERAL_SUBJECT = "SecureCapita - Notification";
    private static final String MFA_SUBJECT = "SecureCapita - Verification Code";
    private static final String RESET_PASSWORD_SUBJECT = "SecureCapita - Reset Password";
    private static final String ENCODING = "UTF-8";

    private final JavaMailSender mailSender;

    @Value("spring.mail.username")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * @param to Email recipient
     * @param message body of the message
     */
    @Async("emailExecutor")
    @Override
    public void sendMessage(String to, String message) {
        this.sendEmail(to, GENERAL_SUBJECT, message);
        LOGGER.info("General email sent to: {}", to);
    }

    /**
     * @param userFirstName
     * @param to Email recipient
     * @param mfaCode Two-Factor Authentication code
     */
    @Async("emailExecutor")
    @Override
    public void sendMfaCode(String userFirstName, String to, String mfaCode) {
        String htmlBody = this.buildMfaEmailBody(userFirstName, mfaCode);
        this.sendEmail(to, MFA_SUBJECT, htmlBody);
        LOGGER.info("Sent MFA Code sent to: {}", to);
    }

    /**
     * @param userFirstName
     * @param to Email recipient
     * @param verificationUrl  Verification URL to reset password
     */
    @Async("emailExecutor")
    @Override
    public void sendResetPasswordUrl(String userFirstName, String to, String verificationUrl) {
        String htmlBody = this.buildResetPasswordEmailBody(userFirstName, verificationUrl);
        this.sendEmail(to, RESET_PASSWORD_SUBJECT, htmlBody);
        LOGGER.info("Reset Password URL sent to {}", to);
    }

    /**
     * Centralizar lógica de envio de emails.
     * */
    private void sendEmail(String to, String subject, String messageBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, ENCODING);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(messageBody, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            LOGGER.error("Failed to send email to {}. Subject: {}", to, subject, e);
            throw new EmailDeliveryFailureException("Error while sending email.", e);
        }
    }

    private String buildMfaEmailBody(String userFirstName, String mfaCode) {
        return String.format(
                """
                    <html>
                        <body style="font-family: Arial, sans-serif; color: #333;">
                            <h1 style="color: #2c3e50;">SecureCapita</h1>
                            <p>Hello, %s</p>
                            <p>Here is your <strong>verification code</strong> to access your account:</p>
                            <div style="margin: 20px 0; padding: 15px; background-color: #f4f4f4; border-radius: 5px; display: inline-block;">
                                <span style="font-size: 24px; font-weight: bold; letter-spacing: 2px; color: #000;">%s</span>
                            </div>
                            <p>If you did not request this code, please ignore this email.</p>
                        </body>
                    </html>
                """,
                userFirstName, mfaCode
        );
    }

    private String buildResetPasswordEmailBody(String userFirstName, String verificationUrl) {
        return String.format(
                """
                    <html>
                        <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e1e1e1; border-radius: 10px;">
                                <h1 style="color: #2c3e50;">SecureCapita</h1>
                                <p>Hello, <strong>%s</strong>,</p>
                                <p>We received a request to reset your password. Click the button below to choose a new one:</p>
                                <div style="text-align: center; margin: 30px 0;">
                                    <a href="%s"
                                       style="background-color: #3498db; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                                       Reset Password
                                    </a>
                                </div>
                                <p>This link will expire in 10 minutes.</p>
                                <p style="font-size: 12px; color: #7f8c8d;">
                                    If the button doesn't work, copy and paste this URL into your browser:<br>
                                    <a href="%s" style="color: #3498db;">%s</a>
                                </p>
                                <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                                <p style="font-size: 12px; color: #bdc3c7;">If you did not request this, you can safely ignore this email.</p>
                            </div>
                        </body>
                    </html>
                """,
                userFirstName, verificationUrl, verificationUrl, verificationUrl
        );
    }
}
