package io.github.joaovitorleal.securecapita.exception;

public class NotFoundRoleByUserIdException extends RuntimeException {
    public NotFoundRoleByUserIdException(String message) {
        super(message);
    }
}
