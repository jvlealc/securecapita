package io.github.jvlealc.securecapita.service;

public interface NotificationService {

    void sendMessage(String to, String message);
    void sendMfaCode(String userFirstName, String to, String mfaCode);
    void sendResetPasswordUrl(String userFirstName, String to, String verificationUrl);
    void sendResetPasswordConfirmationMessage(String userFirstName, String to);
    void sendAccountVerificationUrl(String userFirstName, String to, String verificationUrl);
    void sendAccountVerifiedMessage(String userFirstName, String to);
}
