package io.github.joaovitorleal.securecapita.exception;

public class RoleNotFoundByUserIdException extends RuntimeException {
    public RoleNotFoundByUserIdException(String message) {
        super(message);
    }
}
