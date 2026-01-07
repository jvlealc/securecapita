package io.github.joaovitorleal.securecapita.exception;

public class NotFoundUserByEmailException extends  RuntimeException {
    public NotFoundUserByEmailException(String message) {
        super(message);
    }

    public NotFoundUserByEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
