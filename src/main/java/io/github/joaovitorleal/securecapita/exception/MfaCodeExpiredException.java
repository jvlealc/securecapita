package io.github.joaovitorleal.securecapita.exception;

import org.springframework.security.core.AuthenticationException;

public class MfaCodeExpiredException extends AuthenticationException {

    public MfaCodeExpiredException(String message) {
        super(message);
    }
}
