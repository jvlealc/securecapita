package io.github.joaovitorleal.securecapita.exception;

public class AccountVerificationNotFoundByUrl extends ResourceNotFoundException {

    public AccountVerificationNotFoundByUrl(String message) {
        super(message);
    }
}
