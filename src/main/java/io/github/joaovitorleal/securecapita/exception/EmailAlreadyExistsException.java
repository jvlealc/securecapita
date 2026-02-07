package io.github.joaovitorleal.securecapita.exception;

public class EmailAlreadyExistsException extends ApiException {

    public EmailAlreadyExistsException(String email) {
        super("Email '" + email + "' already exists. Please use a different email and try again.");
    }
}
