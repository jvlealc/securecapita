package io.github.joaovitorleal.securecapita.exception;

public class MfaVerificationNotFoundByUserIdException extends  ApiException {

    public MfaVerificationNotFoundByUserIdException(String message) {
        super(message);
    }
}
