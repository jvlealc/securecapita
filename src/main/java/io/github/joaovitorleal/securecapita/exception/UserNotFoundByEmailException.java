package io.github.joaovitorleal.securecapita.exception;

public class UserNotFoundByEmailException extends ResourceNotFoundException {

    public UserNotFoundByEmailException(String userEmail) {
        super("User not found with email: " + userEmail);
    }
}
