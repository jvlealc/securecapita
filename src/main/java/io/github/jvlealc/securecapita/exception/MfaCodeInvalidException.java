package io.github.jvlealc.securecapita.exception;

import org.springframework.security.core.AuthenticationException;

public class MfaCodeInvalidException extends AuthenticationException {

    public MfaCodeInvalidException(String message) {
        super(message);
    }
}
