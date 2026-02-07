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

    private static final String ENCODING = "UTF-8";
    private static final String GENERAL_SUBJECT = "SecureCapita - Notification";
    private static final String MFA_SUBJECT = "SecureCapita - Verification Code";
    private static final String RESET_PASSWORD_SUBJECT = "SecureCapita - Reset Password";
    private static final String RESET_PASSWORD_CONFIRMATION_SUBJECT = "SecureCapita - Password Changed Successfully";
    private static final String ACCOUNT_VERIFICATION_SUBJECT = "SecureCapita - Verify Your Account";
    private static final String ACCOUNT_VERIFIED_SUBJECT = "SecureCapita - Account Verified Successfully";

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Enviar mensagem de notificações genéricas.
     *
     * @param to Destinatário do email.
     * @param message corpo da mensagem.
     */
    @Async("emailExecutor")
    @Override
    public void sendMessage(String to, String message) {
        this.sendEmail(to, GENERAL_SUBJECT, message);
        LOGGER.info("General email sent to: {}", to);
    }

    /**
     * Enviar de código de autenticação Multifator.
     *
     * @param  userFirstName Primeiro nome do usuário.
     * @param to Destinatário do email.
     * @param mfaCode Código de autenticação Multifator (Two-Factor Authentication).
     */
    @Async("emailExecutor")
    @Override
    public void sendMfaCode(String userFirstName, String to, String mfaCode) {
        String htmlBody = this.buildMfaEmailBody(userFirstName, mfaCode);
        this.sendEmail(to, MFA_SUBJECT, htmlBody);
        LOGGER.info("Sent MFA Code sent to: {}", to);
    }

    /**
     * Enviar URL de verificação de redefinição de senha.
     *
     * @param userFirstName Primeiro nome do usuário.
     * @param to Destinatário do email.
     * @param verificationUrl  URL de verificação para redefinir senha.
     */
    @Async("emailExecutor")
    @Override
    public void sendResetPasswordUrl(String userFirstName, String to, String verificationUrl) {
        String htmlBody = this.buildResetPasswordEmailBody(userFirstName, verificationUrl);
        this.sendEmail(to, RESET_PASSWORD_SUBJECT, htmlBody);
        LOGGER.info("Reset Password URL sent to {}", to);
    }

    /**
     * Enviar mensagem de sucesso na alteração de senha.
     *
     * @param userFirstName Primeiro nome do usuário
     * @param to Destinatário do email.
     */
    @Async("emailExecutor")
    @Override
    public void sendResetPasswordConfirmationMessage(String userFirstName, String to) {
        String htmlBody = this.buildResetPasswordConfirmationBody(userFirstName);
        this.sendEmail(to, RESET_PASSWORD_CONFIRMATION_SUBJECT, htmlBody);
        LOGGER.info("Reset Password Confirmation Message sent to: {}", to);
    }

    /**
     * Enviar URL de ativação de conta.
     *
     * @param userFirstName Primeiro nome do usuário.
     * @param to Destinatário do email.
     * @param verificationUrl URL de ativação de conta.
     */
    @Async("emailExecutor")
    @Override
    public void sendAccountVerificationUrl(String userFirstName, String to, String verificationUrl) {
        String htmlBody = this.buildAccountVerificationBody(userFirstName, verificationUrl);
        this.sendEmail(to, ACCOUNT_VERIFICATION_SUBJECT, htmlBody);
        LOGGER.info("Account verification URL sent to: {}", to);
    }

    /**
     * Envia email de sucesso da ativação de conta
     *
     * @param userFirstName Primeiro nome do usuário
     * @param to Destinatário do email
     */
    @Async("emailExecutor")
    @Override
    public void sendAccountVerifiedMessage(String userFirstName, String to) {
        String htmlBody = this.buildAccountVerifiedBody(userFirstName);
        this.sendEmail(to, ACCOUNT_VERIFIED_SUBJECT, htmlBody);
        LOGGER.info("Account Verified Message sent to: {}", to);
    }

    /**
     * Centralizar lógica de envio de emails.
     *
     * @param to Destinatário do email.
     * @param subject Título do email.
     * @param messageBody corpo do texto ou HTML da mensagem.
     * @throws EmailDeliveryFailureException caso ocorra falha no envio do email.
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

    // Helpers
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

    private String buildResetPasswordConfirmationBody(String userFirstName) {
        return String.format(
                """
                    <html>
                        <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e1e1e1; border-radius: 10px;">
                                <h1 style="color: #2c3e50;">SecureCapita</h1>
                                <p>Hello, <strong>%s</strong>,</p>
                                <p>This is a confirmation that the password for your SecureCapita account has been successfully changed.</p>

                                <div style="background-color: #f0fdf4; border-left: 4px solid #2ecc71; padding: 15px; margin: 20px 0;">
                                    <p style="margin: 0; color: #27ae60;"><strong>✓ Success:</strong> Your account is secured with the new password.</p>
                                </div>
    
                                <p>If you did not perform this action, please contact our support immediately to secure your account.</p>

                                <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                                <p style="font-size: 12px; color: #bdc3c7;">SecureCapita Security Team</p>
                            </div>
                        </body>
                    </html>
                """,
                userFirstName
        );
    }

    private String buildAccountVerificationBody(String userFirstName, String verificationUrl) {
        return String.format(
                """
                    <html>
                        <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e1e1e1; border-radius: 10px;">
                                <h1 style="color: #2c3e50;">SecureCapita</h1>
                                <p>Hello, <strong>%s</strong>,</p>
                                <p>Welcome to SecureCapita! To start using your account, please verify your email address by clicking the button below:</p>
                                <div style="text-align: center; margin: 30px 0;">
                                    <a href="%s"
                                       style="background-color: #3498db; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                                       Verify Account
                                    </a>
                                </div>
                                <p style="font-size: 12px; color: #7f8c8d;">
                                    If the button doesn't work, copy and paste this URL into your browser:<br>
                                    <a href="%s" style="color: #3498db;">%s</a>
                                </p>
                                <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                                <p style="font-size: 12px; color: #bdc3c7;">If you did not create an account, no further action is required.</p>
                            </div>
                        </body>
                    </html>
                """,
                userFirstName, verificationUrl, verificationUrl, verificationUrl
        );
    }

    private String buildAccountVerifiedBody(String userFirstName) {
        return String.format(
                """
                    <html>
                        <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e1e1e1; border-radius: 10px;">
                                <h1 style="color: #2c3e50;">SecureCapita</h1>
                                <p>Hello, <strong>%s</strong>,</p>
                                <p>Great news! Your account has been successfully verified.</p>

                                <div style="background-color: #f0fdf4; border-left: 4px solid #2ecc71; padding: 15px; margin: 20px 0;">
                                    <p style="margin: 0; color: #27ae60;"><strong>✓ Account Activated:</strong> You can now log in and access all features.</p>
                                </div>
    
                                <p>Thank you for joining SecureCapita.</p>

                                <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                                <p style="font-size: 12px; color: #bdc3c7;">SecureCapita Team</p>
                            </div>
                        </body>
                    </html>
                """,
                userFirstName
        );
    }
}
