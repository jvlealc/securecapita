package io.github.joaovitorleal.securecapita.exception;

public class UserNotFoundByIdException extends ResourceNotFoundException {

    public UserNotFoundByIdException(String message) {
        super(message);
    }
}
