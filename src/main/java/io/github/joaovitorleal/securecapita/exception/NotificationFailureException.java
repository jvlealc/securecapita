package io.github.joaovitorleal.securecapita.exception;

public abstract class NotificationFailureException extends RuntimeException {
    public NotificationFailureException(String message) {
        super(message);
    }

    public NotificationFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
