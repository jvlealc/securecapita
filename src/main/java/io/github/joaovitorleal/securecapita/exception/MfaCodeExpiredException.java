package io.github.joaovitorleal.securecapita.exception;

public class MfaCodeExpiredException extends ApiException {

    public MfaCodeExpiredException(String message) {
        super(message);
    }
}
