package io.github.joaovitorleal.securecapita.exception;

public class UserNotFoundByEmailException extends  RuntimeException {
    public UserNotFoundByEmailException(String message) {
        super(message);
    }

    public UserNotFoundByEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
