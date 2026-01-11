package io.github.joaovitorleal.securecapita.exception;

public class EmailDeliveryFailureException extends NotificationFailureException {

    public EmailDeliveryFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
