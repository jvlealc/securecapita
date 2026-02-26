package io.github.jvlealc.securecapita.exception;

import javax.naming.AuthenticationException;

public class MfaVerificationNotFoundByUserIdException extends AuthenticationException {

    public MfaVerificationNotFoundByUserIdException(String message) {
        super(message);
    }
}
