package io.github.jvlealc.securecapita.exception;

public class EmailDeliveryFailureException extends NotificationFailureException {

    public EmailDeliveryFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
