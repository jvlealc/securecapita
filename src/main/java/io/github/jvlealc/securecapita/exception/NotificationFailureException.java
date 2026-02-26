package io.github.jvlealc.securecapita.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public abstract class NotificationFailureException extends ApiException {

    public NotificationFailureException(String message) {
        super(message);
    }

    public NotificationFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
