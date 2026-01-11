package io.github.joaovitorleal.securecapita.exception;

public class MfaDeliveryFailureException extends NotificationFailureException {
    public MfaDeliveryFailureException(String message) {
        super(message);
    }

    public MfaDeliveryFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
